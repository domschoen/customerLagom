package services

import client.Main.LoginLoc
import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom

import scala.concurrent.Future
import client.{Customer, CustomerEvent, CustomerRenamed, CustomerCreated}

// Actions
case object UseLocalStorageUser extends Action
case class LoginWithID(userId: String) extends Action
case class RegisterCustomers(customers: Option[List[Customer]]) extends Action
case object InitApp extends Action
case class CustomerRenamedReceived(customerRenamed: CustomerRenamed) extends Action
case class CustomerCreatedReceived(customerCreated: CustomerCreated) extends Action

case object Logout extends Action
case object FetchCustomers extends Action



// The base model of our application
case class UserLogin(loginChecked: Boolean = false)
case class MegaContent(userLogin: UserLogin, allCustomers: Option[List[Customer]], customerEvents: List[CustomerEvent])
case class RootModel(content: MegaContent)

/**
  * Handles actions
  *
  * @param modelRW Reader/Writer to access the model
  */
class UserLoginHandler[M](modelRW: ModelRW[M, UserLogin]) extends ActionHandler(modelRW) {
  override def handle = {
    case InitApp =>
      println("UsersHandler | InitApp | ")
      StreamUtils.createStream()
      noChange


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

class CustomerEventHandler[M](modelRW: ModelRW[M, List[CustomerEvent]]) extends ActionHandler(modelRW) {
  override def handle = {
    case CustomerCreatedReceived (event) =>
      updated(event :: value)
    case CustomerRenamedReceived (event) =>
      updated(event :: value)
  }
}

// Application circuit
object SPACircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // initial application model
  override protected def initialModel = RootModel(MegaContent(UserLogin(false), None, List()))
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new UserLoginHandler(zoomTo(_.content.userLogin)),
    new AllCustomersHandler(zoomTo(_.content.allCustomers)),
    new CustomerEventHandler(zoomTo(_.content.customerEvents))
  )
}