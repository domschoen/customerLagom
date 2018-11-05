package com.nagravision.customer.impl

import java.util.UUID

import akka.actor.ActorSystem
import akka.persistence.query.PersistenceQuery
import com.nagravision.customer.api
import com.nagravision.customer.api.CustomerService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.lightbend.lagom.scaladsl.api.transport.{Forbidden, NotFound}
import akka.{Done, NotUsed}

import scala.concurrent.{ExecutionContext, Future}
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.broker.Topic.TopicId
/**
  * Implementation of the CustomerService.
  */
class CustomerServiceImpl(registry: PersistentEntityRegistry, customerRepository: CustomerRepository, system: ActorSystem) (implicit ec: ExecutionContext, mat: Materializer) extends CustomerService {


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

  def customerEventsTopic(): Topic[api.CustomerEvent] =
    TopicProducer.taggedStreamWithOffset(CustomerEvent.Tag.allTags.toList) { (tag, offset) =>
      registry.eventStream(tag, offset)
        .filter {
          _.event match {
            case x@(_: CustomerCreated | _: CustomerRenamed ) => true
            case _ => false
          }
        }.mapAsync(1)(convertEvent)
    }

  override def getLiveCustomerEvents(trigram: String): ServiceCall[NotUsed, Source[CustomerEvent, NotUsed]] = {
    _ =>
      Future {
        customerEventsTopic()
          .subscribe
          .atMostOnceSource
          .mapAsync(1)
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


  private def convertEvent(customerEvent: EventStreamElement[CustomerEvent]): api.CustomerEvent = {
    customerEvent.event match {
      case CustomerCreated(customer) => api.CustomerCreated(customer.trigram,
        customer.name,
        customer.customerType,
        customer.dynamicsAccountID,
        customer.headCountry,
        customer.region
      )
      case CustomerRenamed(newName) => api.CustomerRenamed(customerEvent.entityId, newName)
    }
  }

  private def convertEvent(eventStreamElement: EventStreamElement[ItemEvent]): Future[(api.ItemEvent, Offset)] = {
    eventStreamElement match {
      case EventStreamElement(itemId, AuctionStarted(_), offset) =>
        entityRefString(itemId).ask(GetItem).map {
          case Some(item) =>
            (api.AuctionStarted(
              itemId = item.id,
              creator = item.creator,
              reservePrice = item.reservePrice,
              increment = item.increment,
              startDate = item.auctionStart.get,
              endDate = item.auctionEnd.get
            ), offset)
        }
      case EventStreamElement(itemId, AuctionFinished(winner, price), offset) =>
        entityRefString(itemId).ask(GetItem).map {
          case Some(item) =>
            (api.AuctionFinished(
              itemId = item.id,
              item = convertItem(item)
            ), offset)
        }
    }
  }

  private def convertCustomer(customer: Customer): api.Customer = {
    api.Customer(Some(customer.trigram), customer.name, customer.customerType, customer.dynamicsAccountID, customer.headCountry, customer.region)
  }


  private def entityRef(trigram: String) = entityRefString(trigram)

  private def entityRefString(trigram: String) = registry.refFor[CustomerEntity](trigram)

}
