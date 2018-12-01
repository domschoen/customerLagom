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
import client.Main.Loc
import client.Customer
import components.NewCustomer.State
import upickle.default.{write, macroRW, ReadWriter => RW}




case class CustomerNewName(name: String)

object CustomerNewName{
  implicit def rw: RW[CustomerNewName] = macroRW
}



object ShowCustomer {

  case class Props(ctl: RouterCtl[Loc], proxy: ModelProxy[MegaContent], customer: Customer)
  case class State(name: Option[String], error: Option[String])


  protected class Backend($: BackendScope[Props, State]) {


    def handleSubmit(p: Props, s: State, e: ReactEventFromInput, customer: Customer): Callback = {
      e.preventDefaultCB >> {
        val customerNewName = CustomerNewName(s.name.get)
        val request = Ajax.post(
          url = "/api/customer/" + customer.trigram + "/rename",
          data = write(customerNewName)
        ).recover {
          // Recover from a failed error code into a successful future
          case dom.ext.AjaxException(req) => req
        }.map(r =>
          r.status match {
            case 200 =>
              Callback.empty

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

    def render(p: Props, s: State): VdomElement = {
      //println("render | AppPage")
        val customer = p.customer
        val nameString = if (s.name.isDefined) s.name.get else ""
        <.div(
          <.form(^.className := "signupForm", ^.onSubmit ==> { e: ReactEventFromInput => handleSubmit(p, s, e, customer)},


            <.table(
              <.tbody(
                <.tr(
                  <.th("NAME"),
                  <.td(<.input.text(^.placeholder := "Name...", ^.value := nameString,
                    ^.onChange ==> { e: ReactEventFromInput => handleNameChange(e)})),
                  <.td(<.input.submit(^.value := "Rename"))
                )
              )
            )
          ),
          <.h2("Action Events:"), //.when(customer.isDefined),
          <.table(
            <.tbody(
              p.proxy.value.customerEvents toTagMod (
                customerEvent => {
                  <.tr(
                    <.td(customerEvent.toString)
                  )
                }
                )
            )
          ) //.when(customer.isDefined)

        )
    }
  }

  def initName(customerOpt: Option[Customer]) = {
    println("initName() customerOpt " + customerOpt)
    customerOpt match {
      case Some(customer) => Some(customer.name)
      case None => None
    }
  }

  // create the React component for Dashboard
  private val component = ScalaComponent.builder[Props]("AppPage")
    .initialStateFromProps(p => State(initName(p.proxy.value.customer.customer), None))
    .renderBackend[Backend]
    .build

  def apply(ctl: RouterCtl[Loc], proxy: ModelProxy[MegaContent], customer: Customer) = {
    component(Props(ctl, proxy, customer))
  }
}
