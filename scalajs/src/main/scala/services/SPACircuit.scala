package services

import client.Main.LoginLoc
import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.scalajs.dom

import scala.concurrent.Future

// Actions
case object UseLocalStorageUser extends Action
case class LoginWithID(userId: String) extends Action
case class RegisterFriends(friendIDs: List[String]) extends Action
case object InitApp extends Action

case object Logout extends Action

// The base model of our application
case class UserLogin(loginChecked: Boolean = false)
case class MegaContent(userLogin: UserLogin)
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



// Application circuit
object SPACircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // initial application model
  override protected def initialModel = RootModel(MegaContent(UserLogin(false)))
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new UserLoginHandler(zoomTo(_.content.userLogin))
  )
}