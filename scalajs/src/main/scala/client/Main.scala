package client

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import components.AppPage
import com.zoepepper.facades.jsjoda._
import services.{InitApp, MegaContent, SPACircuit, UseLocalStorageUser}
import diode.Action
import diode.react.ModelProxy
import components.AppPage
import japgolly.scalajs.react.extra.router.{Redirect, RouterConfigDsl}
import org.scalajs.dom

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("Main")
object Main {

  sealed trait Loc
  case object LoginLoc extends Loc
  case object SignupLoc extends Loc
  case object AddFriendLoc extends Loc
  case class UserChirpLoc(userId: String) extends Loc


  // configure the router
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._
    val wrapper = SPACircuit.connect(_.content)

    val contentWrapper = SPACircuit.connect(_.content)


    (emptyRule
      | staticRoute(root, LoginLoc) ~> renderR(ctl => contentWrapper(AppPage(ctl, _, None, false)))
      ).notFound(redirectToPage(LoginLoc)(Redirect.Replace))

  }

  def main(args: Array[String]): Unit = {
    val router = Router(BaseUrl.until_#, routerConfig)

    //SPACircuit.dispatch(InitApp)

    router().renderIntoDOM(dom.document.getElementById("root"))
  }
}
