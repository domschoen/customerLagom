package com.nagravision.customer.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}
import com.nagravision.customer.utils.JsonFormats._

import scala.collection.immutable.Seq

class CustomerEntity extends PersistentEntity {

  override type Command = CustomerCommand[_]
  override type Event = CustomerEvent
  override type State = Option[Customer]

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: Option[Customer] = None

  private val getCustomerCommand = Actions().onReadOnlyCommand[GetCustomer.type, Option[Customer]] {
    case (GetCustomer, ctx, state) => ctx.reply(state)
  }

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {

    case None => {
      Actions().onCommand[CreateCustomer, Done] {
        case (CreateCustomer(customer), ctx, state) =>
          ctx.thenPersist(CustomerCreated(customer))(_ => ctx.reply(Done))

      }.onEvent {
        case (CustomerCreated(customer), state) => Some(customer)
      }.orElse(getCustomerCommand)

    }

    case Some(customer) => Actions().onCommand[RenameCustomer, Done] {

      // Command handler for the UseGreetingMessage command
      case (RenameCustomer(newName), ctx, state) =>
        // In response to this command, we want to first persist it as a
        // GreetingMessageChanged event
        ctx.thenPersist(
          CustomerRenamed(newName)
        ) { _ =>
          // Then once the event is successfully persisted, we respond with done.
          ctx.reply(Done)
        }
    }.onEvent {
      case (CustomerRenamed(newName), state) => {
        val currentCustomer = state.get
        val updatedCustomer = currentCustomer.copy(name = newName)
        Some(updatedCustomer)
      }
    }.orElse(getCustomerCommand)

  }

}

case class Customer (
                      trigram: String,
                      name: String,
                      customerType: String,
                      dynamicsAccountID: String,
                      headCountry: String,
                      region: String
                    )

object Customer {
  implicit val format: Format[Customer] = Json.format
}


/// Commands

sealed trait CustomerCommand[R] extends ReplyType[R]


case object GetCustomer extends CustomerCommand[Option[Customer]] {
  implicit val format: Format[GetCustomer.type] = singletonFormat(GetCustomer)
}


case class CreateCustomer(customer: Customer) extends CustomerCommand[Done]

object CreateCustomer {
  implicit val format: Format[CreateCustomer] = Json.format
}

case class RenameCustomer(name: String) extends CustomerCommand[Done]

object RenameCustomer {
  implicit val format: Format[RenameCustomer] = Json.format
}



/// Events
sealed trait CustomerEvent extends AggregateEvent[CustomerEvent] {
  def aggregateTag = CustomerEvent.Tag
}

object CustomerEvent {
  val Tag = AggregateEventTag[CustomerEvent]
}

/**
  * An event that represents a change in greeting message.
  */
case class CustomerCreated(customer: Customer) extends CustomerEvent

object CustomerCreated {
  implicit val format: Format[CustomerCreated] = Json.format
}

case class CustomerRenamed(name: String) extends CustomerEvent

object CustomerRenamed {
  implicit val format: Format[CustomerRenamed] = Json.format
}


object CustomerSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[CreateCustomer],
    JsonSerializer[RenameCustomer]
  )
}

