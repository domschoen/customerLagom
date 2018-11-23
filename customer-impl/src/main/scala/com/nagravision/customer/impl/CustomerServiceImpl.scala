package com.nagravision.customer.impl

import java.util.UUID

import akka.actor.ActorSystem
import akka.persistence.query.{EventEnvelope, Offset, PersistenceQuery}
import com.nagravision.customer.api
import com.nagravision.customer.api.{CustomerEvent, CustomerService, LiveCustomerEventsRequest}
import com.lightbend.lagom.scaladsl.api.{ServiceCall, ServiceLocator}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.lightbend.lagom.scaladsl.api.transport.{Forbidden, NotFound}
import akka.{Done, NotUsed}

import scala.concurrent.{ExecutionContext, Future}
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.serialization.MessageSerializer
import akka.stream.Materializer
import akka.stream.javadsl.{Concat, Keep}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.lightbend.lagom.internal.broker.kafka.NoKafkaBrokersException
//import com.lightbend.lagom.scaladsl.api.broker.Topic.TopicId
import javax.inject.Inject
import kafka.server.KafkaConfig
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import com.lightbend.lagom.scaladsl.pubsub.TopicId

/**
  * Implementation of the CustomerService.
  */
//
class CustomerServiceImpl(registry: PersistentEntityRegistry,   pubSub: PubSubRegistry,
                          customerService: CustomerService, customerRepository: CustomerRepository, system: ActorSystem) (implicit ec: ExecutionContext, mat: Materializer) extends CustomerService {

  private val currentIdsQuery = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)


  def createTrigram = {
    "NNN"
  }


  // With not shard seems to not be ok with Cassandra ????
/*  override def customerEventsTopic(): Topic[api.CustomerEvent] = {
    TopicProducer.singleStreamWithOffset {
      offset =>
        registry.eventStream(CustomerEvent.Tag, offset)
          .map(ev => (convertToApiCustomerEvent(ev), offset))

    }
  }*/


  override def customerEventsTopic(): Topic[api.CustomerEvent] =
    TopicProducer.taggedStreamWithOffset(CustomerEvent.Tag.allTags.toList) { (tag, offset) =>
      registry.eventStream(tag, Offset.noOffset)
        .filter {
          _.event match {
            case x@(_: CustomerCreated | _: CustomerRenamed ) => true
            case _ => false
          }
        }.mapAsync(2)(convertEvent)
    }




  /*override def getLiveAllCustomerEvents(): ServiceCall[NotUsed, Source[api.CustomerEvent, NotUsed]] = ServiceCall { _ =>
    val source = customerService.customerEventsTopic.subscribe.atMostOnceSource
    val newSource: Source[api.CustomerEvent, NotUsed] =  Source.fromGraph(source)
      .mapMaterializedValue(ev => NotUsed.getInstance())
    Future.successful(newSource)
  }*/




  override def currentPersistenceIds(): ServiceCall[NotUsed, Source[String, NotUsed]] = { _ => {
    //Future.successful(currentIdsQuery.currentPersistenceIds())
    Future.successful(currentIdsQuery.eventsByPersistenceId("CustomerEntity|NZZ", 0, Long.MaxValue).mapAsync(1)(
      ev => Future(ev.event.toString())))

  }
  }

  def getLiveCustomerNewEvents(): ServiceCall[NotUsed, Source[api.CustomerEvent, NotUsed]] = { _ => {
    val topicCustomerRenamed = pubSub.refFor(TopicId[api.CustomerRenamed])
    val liveCustomerRenamedSource = topicCustomerRenamed.subscriber

    Future.successful(liveCustomerRenamedSource)
  }
  }


  override def getLiveCustomerEvents(): ServiceCall[LiveCustomerEventsRequest, Source[api.CustomerEvent, NotUsed]] = { req => {
    val topicCustomerRenamed = pubSub.refFor(TopicId[api.CustomerRenamed])
    val liveCustomerRenamedSource = topicCustomerRenamed.subscriber

    val topicCostomerCreated = pubSub.refFor(TopicId[api.CustomerCreated])
    val liveCustomerCreatedSource = topicCostomerCreated.subscriber

    val persistenceId = "CustomerEntity|" + req.trigram
    val historicalEventSource = currentIdsQuery.eventsByPersistenceId(persistenceId, 0, Long.MaxValue).mapAsync(1)(
      ev => Future({
        val event = ev.event
        convertImplEventToApiEvent(req.trigram, event.asInstanceOf[CustomerEvent])

      }
      ))
    val combinedSource = historicalEventSource.concat(liveCustomerCreatedSource)
    Future.successful(combinedSource)
  }
  }

  override def createCustomer = ServiceCall[api.Customer, Done] { customer => {
    val trigram = customer.trigram match {
    case Some(trigramValue) => trigramValue
    case None => createTrigram
    }
    val c = Customer(trigram, customer.name, customer.customerType, customer.dynamicsAccountID, customer.headCountry, customer.region)

    println("c " + c)
    // Ask the entity the Hello command.
    val reply = entityRef(trigram).ask(CreateCustomer(c))
    reply.map(ack => {
    val topic = pubSub.refFor(TopicId[api.CustomerCreated])
    topic.publish(api.CustomerCreated(trigram, customer.name, customer.customerType, customer.dynamicsAccountID, customer.headCountry, customer.region))
    Done
  })

  }
}


  override def renameCustomer(trigram: String) = ServiceCall { request =>
    val ref = entityRef(trigram)


    val reply = ref.ask(RenameCustomer(request.name))
    reply.map(ack => {
      val topic = pubSub.refFor(TopicId[api.CustomerRenamed])
      topic.publish(api.CustomerRenamed(trigram, request.name))
      Done
    })

  }


  override def getCustomer(trigram: String) = ServiceCall { _ =>
    entityRef(trigram).ask(GetCustomer).map {
      case Some(customer) => convertCustomer(customer)
      case None => throw NotFound("Customer with trigram " + trigram + " not found");
    }
  }

  override def getCustomers = ServiceCall { _ =>
    customerRepository.getCustomers.map(customers => customers.map(convertCustomer))
  }

  private def convertImplEventToApiEvent(trigram: String, event: CustomerEvent) : api.CustomerEvent = {
    event match {
      case CustomerCreated(customer) =>
        api.CustomerCreated(customer.trigram,
          customer.name,
          customer.customerType,
          customer.dynamicsAccountID,
          customer.headCountry,
          customer.region
        )


      case CustomerRenamed(newName) =>
        api.CustomerRenamed(trigram, newName)
    }
  }


  private def convertToApiCustomerEvent(eventStreamElement: EventStreamElement[CustomerEvent]): api.CustomerEvent = {
    eventStreamElement match {
      case EventStreamElement(trigram, CustomerCreated(customer), offset) =>
        api.CustomerCreated(customer.trigram,
          customer.name,
          customer.customerType,
          customer.dynamicsAccountID,
          customer.headCountry,
          customer.region
        )


      case EventStreamElement(trigram, CustomerRenamed(newName), offset) =>
        api.CustomerRenamed(trigram, newName)

    }
  }


  private def convertEvent(eventStreamElement: EventStreamElement[CustomerEvent]): Future[(api.CustomerEvent, Offset)] = {
    eventStreamElement match {
      case EventStreamElement(trigram, CustomerCreated(customer), offset) =>
        Future.successful {
          (api.CustomerCreated(customer.trigram,
            customer.name,
            customer.customerType,
            customer.dynamicsAccountID,
            customer.headCountry,
            customer.region
          ), offset)
        }

      case EventStreamElement(trigram, CustomerRenamed(newName), offset) =>
        Future.successful {
          (api.CustomerRenamed(trigram, newName), offset)
        }
    }
  }

  private def convertCustomer(customer: Customer): api.Customer = {
    api.Customer(Some(customer.trigram), customer.name, customer.customerType, customer.dynamicsAccountID, customer.headCountry, customer.region)
  }


  private def entityRef(trigram: String) = entityRefString(trigram)

  private def entityRefString(trigram: String) = registry.refFor[CustomerEntity](trigram)

}
