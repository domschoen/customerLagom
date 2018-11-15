package com.nagravision.customer.impl

import java.util.UUID

import akka.actor.ActorSystem
import akka.persistence.query.{Offset, PersistenceQuery}
import com.nagravision.customer.api
import com.nagravision.customer.api.CustomerService
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
import akka.stream.javadsl.Keep
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.lightbend.lagom.internal.broker.kafka.NoKafkaBrokersException
import com.lightbend.lagom.scaladsl.api.broker.Topic.TopicId
import javax.inject.Inject
import kafka.server.KafkaConfig
/**
  * Implementation of the CustomerService.
  */
// customerService: CustomerService,
class CustomerServiceImpl(registry: PersistentEntityRegistry,  customerRepository: CustomerRepository, system: ActorSystem) (implicit ec: ExecutionContext, mat: Materializer) extends CustomerService {

  private val currentIdsQuery = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)


  def createTrigram = {
    "NNN"
  }



  /*def customerEventsTopic(): Topic[api.CustomerEvent] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        registry.eventStream(CustomerEvent.Tag.allTags, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }*/

  override def customerEventsTopic: Topic[api.CustomerEvent] =
    TopicProducer.taggedStreamWithOffset(CustomerEvent.Tag.allTags.toList) { (tag, offset) =>
      registry.eventStream(tag, offset)
        .filter {
          _.event match {
            case x@(_: CustomerCreated | _: CustomerRenamed ) => true
            case _ => false
          }
        }.mapAsync(1)(convertEvent)
    }



  // def sayHello: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  //val dematerializingSink =

  override def getLiveCustomerEvents: ServiceCall[NotUsed, Source[api.CustomerEvent, NotUsed]] = ServiceCall { _ =>
    // Source[CustomerEvent, _] does not conform to Source[CustomerEvent, NotUsed]
    //Future.successful(customerEventsTopic.subscribe.atMostOnceSource.mapMaterializedValue(_ => NotUsed))

    //val source: Source[api.CustomerEvent, _] = customerEventsTopic.subscribe
    //val flow = Flow(source)
    //val identity = Flow[api.CustomerEvent]
    //val flow = source.viaMat(identity)
    //val toto = Flow.fromSinkAndSource[Any, Int](Sink.ignore, flow)

    //Future.successful(source.mapAsync(1)(x => Future(x))
    //Future.successful(source.mapAsync(1)(x => Future(x))
    val source = customerEventsTopic.subscribe.atMostOnceSource
    val newSource: Source[api.CustomerEvent, NotUsed] =  Source.fromGraph(source)
      .mapMaterializedValue(ev => NotUsed.getInstance())
    Future.successful(newSource)

    //Future.successful(customerEventsTopic.subscribe.atLeastOnce(Flow[api.CustomerEvent].map {ev => ev}))
  }



  /*override def getLiveCustomerEvents: ServiceCall[NotUsed, Source[api.CustomerEvent, _]] = ServiceCall { _ =>
    // Source[CustomerEvent, _] does not conform to Source[CustomerEvent, NotUsed]
    //Future.successful(customerEventsTopic.subscribe.atMostOnceSource.mapMaterializedValue(_ => NotUsed))

    //val source: Source[api.CustomerEvent, _] = customerEventsTopic.subscribe.atLeastOnce
    //val flow = Flow(source)
    val identity = Flow[api.CustomerEvent]
    //val flow = source.viaMat(identity)
    //val toto = Flow.fromSinkAndSource[Any, Int](Sink.ignore, flow)

    //Future.successful(source.mapAsync(1)(x => Future(x))
    Future(customerEventsTopic.subscribe.atMostOnceSource)
  }*/

  /*  ServiceCall[NotUsed, Source[CustomerEvent, NotUsed]] { _ =>
        Future (customerEventsTopic()
          .subscribe
          .atMostOnceSource
        )
  }*/

  override def createCustomer = ServiceCall[api.Customer, Done] { customer => {
      val trigram = customer.trigram match {
        case Some(trigramValue) => trigramValue
        case None => createTrigram
      }
      val c = Customer(trigram, customer.name, customer.customerType, customer.dynamicsAccountID, customer.headCountry, customer.region)

      println("c " + c)
      // Ask the entity the Hello command.
      entityRef(trigram).ask(CreateCustomer(c))
    }
  }


  override def renameCustomer(trigram: String) = ServiceCall { request =>
    val ref = entityRef(trigram)
    ref.ask(RenameCustomer(request.name))
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
