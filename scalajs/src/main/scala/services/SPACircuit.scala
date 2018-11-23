package services

import client.Main.LoginLoc
import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom

import scala.concurrent.Future
import client.{Customer, CustomerEventType, CustomerEvent}
import client.CustomerEvent.{CustomerRenamed, CustomerCreated}

// Actions
case object UseLocalStorageUser extends Action
case class LoginWithID(userId: String) extends Action
case class RegisterCustomers(customers: Option[List[Customer]]) extends Action
case class SetCustomer(customer: Customer) extends Action

case object Logout extends Action
case object FetchCustomers extends Action


case class InspectCustomer(customer: Customer) extends Action

// The base model of our application
case class UserLogin(loginChecked: Boolean = false)
case class MegaContent(userLogin: UserLogin, customer: Option[Customer], allCustomers: Option[List[Customer]], customerEvents: List[CustomerEvent])
case class RootModel(content: MegaContent)

/**
  * Handles actions
  *
  * @param modelRW Reader/Writer to access the model
  */
class UserLoginHandler[M](modelRW: ModelRW[M, UserLogin]) extends ActionHandler(modelRW) {
  override def handle = {
    case UseLocalStorageUser =>
      noChange

  }
}

class AllCustomersHandler[M](modelRW: ModelRW[M, Option[List[Customer]]]) extends ActionHandler(modelRW) {
  override def handle = {
    case FetchCustomers =>
      effectOnly(Effect(CustomerUtils.getCustomers().map(RegisterCustomers(_))))

    case RegisterCustomers(customersOpt) =>
      customersOpt match {
        case Some(customers) =>
          updated(Some(customers))
        case None =>
          updated(None)

      }
  }
  }


class CustomerHandler[M](modelRW: ModelRW[M, Option[Customer]]) extends ActionHandler(modelRW) {
  override def handle = {
    case InspectCustomer(customer: Customer) =>
      updated(Some(customer),Effect.action(SetCustomer(customer)))
  }
}

class CustomerEventHandler[M](modelRW: ModelRW[M, List[CustomerEvent]]) extends ActionHandler(modelRW) {
  override def handle = {
    case SetCustomer(customer) =>
      StreamUtils.createStream(customer.trigram)
      updated(List())

    case CustomerCreated(
      trigram: String,
      name: String,
      customerType: String,
      dynamicsAccountID: String,
      headCountry: String,
      region: String,
      event_type: String
    ) =>
      updated(CustomerCreated(trigram, name,customerType,dynamicsAccountID,headCountry,region,event_type) :: value)
    case CustomerRenamed (
      trigram: String,
      name: String,
      event_type: String
    ) =>
      updated(CustomerRenamed(trigram,name,event_type) :: value)
  }
}

// Application circuit
object SPACircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // initial application model
  override protected def initialModel = RootModel(MegaContent(UserLogin(false), None, None, List()))
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new UserLoginHandler(zoomTo(_.content.userLogin)),
    new AllCustomersHandler(zoomTo(_.content.allCustomers)),
    new CustomerHandler(zoomTo(_.content.customer)),
    new CustomerEventHandler(zoomTo(_.content.customerEvents))
  )
}