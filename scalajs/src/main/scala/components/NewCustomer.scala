package components

import scala.util.Random
import scala.language.existentials
import org.scalajs.dom
import org.scalajs.dom.Event

import scala.util.{Failure, Random, Success}
import scala.language.existentials
import org.scalajs.dom

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.typedarray._
import org.scalajs.dom.ext.AjaxException
import dom.ext.Ajax
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import diode.Action
import diode.react.ModelProxy
import services._
import client.Main.{Loc, LoginLoc}
import client.Customer
import upickle.default.write
import upickle.default.{macroRW, ReadWriter => RW}



// Translation of App
object NewCustomer {

  case class Props(ctl: RouterCtl[Loc], proxy: ModelProxy[MegaContent])

  case class State(trigram: Option[String], name: Option[String], dynamicsAccountID: Option[String], headCountry: Option[String], region: Option[String], error: Option[String])

  protected class Backend($: BackendScope[Props, State]) {

    def handleSubmit(p: Props, s: State, e: ReactEventFromInput): Callback = {
      e.preventDefaultCB >> {
        val customer = Customer(s.trigram.get, s.name.get, "Operator", s.dynamicsAccountID.get, s.headCountry.get, s.region.get)
        val request = Ajax.post(
          url = "/api/customer",
          data = write(customer)
        ).recover {
          // Recover from a failed error code into a successful future
          case dom.ext.AjaxException(req) => req
        }.map(r =>
          r.status match {
            case 200 =>
              Callback.log(s"Successful creation of: $customer") >>
                $.props >>= (_.proxy.dispatchCB(CustomerHasBeenCreated))

            case _ =>
              val errorMsg = "Customer already exist."
              println(errorMsg)
              $.modState(_.copy(error = Some(errorMsg)))
          }
        )
        Callback.future(request)
      }
    }
    def handleNameChange(e: ReactEventFromInput) = {
      val newName = if (e.target.value == null) None else Some(e.target.value)
      $.modState(_.copy(name = newName))
    }

    def handleTrigramChange(e: ReactEventFromInput) = {
      val newName = if (e.target.value == null) None else Some(e.target.value)
      $.modState(_.copy(trigram = newName))
    }
    def handleDynamicsAccountIDChange(e: ReactEventFromInput) = {
      val newName = if (e.target.value == null) None else Some(e.target.value)
      $.modState(_.copy(dynamicsAccountID = newName))
    }
    def handleHeadCountryChange(e: ReactEventFromInput) = {
      val newName = if (e.target.value == null) None else Some(e.target.value)
      $.modState(_.copy(headCountry = newName))
    }
    def handleRegionChange(e: ReactEventFromInput) = {
      val newName = if (e.target.value == null) None else Some(e.target.value)
      $.modState(_.copy(region = newName))
    }

    def render(p: Props, s: State): VdomElement = {
      //println("render | AppPage")
      val show = p.proxy.value.customer.addCustomer

      if (show) {
        val trigramString = if (s.trigram.isDefined) s.trigram.get else ""
        val nameString = if (s.name.isDefined) s.name.get else ""
        val dynamicsAccountIDString = if (s.dynamicsAccountID.isDefined) s.dynamicsAccountID.get else ""
        val headCountryString = if (s.headCountry.isDefined) s.headCountry.get else ""
        val regionString = if (s.region.isDefined) s.region.get else ""
        val errorMsg = if (s.error.isDefined) s.error.get else ""

        <.div(
          <.h3("Add Customer"),
          <.form(^.className := "signupForm", ^.onSubmit ==> { e: ReactEventFromInput => handleSubmit(p, s, e)},
            <.input.text(^.placeholder := "Name...", ^.value := nameString,
              ^.onChange ==> { e: ReactEventFromInput => handleNameChange(e)}),
            <.input.text(^.placeholder := "Trigram...", ^.value := trigramString,
              ^.onChange ==> { e: ReactEventFromInput => handleTrigramChange(e)}),
            <.input.text(^.placeholder := "Dynamics Account ID...", ^.value := dynamicsAccountIDString,
              ^.onChange ==> { e: ReactEventFromInput => handleDynamicsAccountIDChange(e)}),
            <.input.text(^.placeholder := "Head Country...", ^.value := headCountryString,
              ^.onChange ==> { e: ReactEventFromInput => handleHeadCountryChange(e)}),
            <.input.text(^.placeholder := "Region...", ^.value := regionString,
              ^.onChange ==> { e: ReactEventFromInput => handleRegionChange(e)}),
            {
              s.error
            }.when(s.error.isDefined),
            <.input.submit(^.value := "Save")
          )
        )
      } else {
        <.div()
      }
    }
  }
  // create the React component for Dashboard
  private val component = ScalaComponent.builder[Props]("AppPage")
    .initialState(State(None, None,  None, None, None,  None))
    .renderBackend[Backend]
    .build

  def apply(ctl: RouterCtl[Loc], proxy: ModelProxy[MegaContent]) = {
    component(Props(ctl, proxy))
  }
}
