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

// Translation of App
object ShowCustomer {

  case class Props(ctl: RouterCtl[Loc], proxy: ModelProxy[MegaContent])


  protected class Backend($: BackendScope[Props, Unit]) {

    def render(p: Props): VdomElement = {
      println("render | AppPage")
      val customerOpt = p.proxy.value.customer.customer
      val show = customerOpt.isDefined

      if (show) {
        val customer = customerOpt.get
        <.div(
          <.table(
            <.tbody(
              <.tr(
                <.th("NAME"),
                <.td(customer.name)
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
      } else {
        <.div()
      }
    }
  }
  // create the React component for Dashboard
  private val component = ScalaComponent.builder[Props]("AppPage")
    .renderBackend[Backend]
    .build

  def apply(ctl: RouterCtl[Loc], proxy: ModelProxy[MegaContent]) = {
    println("AppPage | apply")
    component(Props(ctl, proxy))
  }
}
