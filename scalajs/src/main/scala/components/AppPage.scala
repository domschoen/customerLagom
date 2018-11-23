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
import services.{FetchCustomers, InspectCustomer, MegaContent, UseLocalStorageUser}
import client.Main.Loc
import client.Customer

// Translation of App
object AppPage {

  case class Props(ctl: RouterCtl[Loc], proxy: ModelProxy[MegaContent], userId: Option[String], showAddFriends: Boolean)


  protected class Backend($: BackendScope[Props, Unit]) {

    def willReceiveProps(currentProps: Props, nextProps: Props): Callback = {
      println("AppPage | willReceiveProps")
      Callback.empty
    }

    def mounted(p: Props): japgolly.scalajs.react.Callback = {
      println("AppPage | mounted")
      p.proxy.dispatchCB(FetchCustomers)
    }

    def inspectEO(customer: Customer) = {
      Callback.log(s"Inspect: $customer") >>
        $.props >>= (_.proxy.dispatchCB(InspectCustomer(customer)))
    }



    def render(p: Props): VdomElement = {
      println("render | AppPage")
      val allCustomersFetched = p.proxy.value.allCustomers.isDefined
      val customer = p.proxy.value.customer
      <.div(^.className := "container body",
        <.div(^.className := "grey-to-blue-background",
          <.h2("CUSTOMER REPOSITORY")
        ),
        p.proxy.value.allCustomers match {
          case Some(customers) =>
            val count = customers.size
            val entityDisplayName = "Customer"
            val countText = count match {
              case x if x == 0 =>
                "No " + entityDisplayName
              case x if x > 1 =>
                entityDisplayName match {
                  case  "Alias" => count + " " + "Aliases"
                  case _ => count + " " + entityDisplayName + "s"
                }
              case _ => count + " " + entityDisplayName
            }

            <.table(^.className := "table table-bordered table-hover table-condensed",
              <.thead(
                <.tr(
                  <.th(^.className := "result-details-header", ^.colSpan := 100,
                    countText,
                  )
                )
              ),
              <.thead(
                <.th(),
                <.th(^.className := "",
                  <.span(^.className := "", "Prefered Name")
                ),
                <.th(^.className := "",
                  <.span(^.className := "", "Dynamics Account ID")
                ),
                <.th(^.className := "",
                  <.span(^.className := "", "Customer Trigram")
                )
              ),
              <.tbody(
                customers toTagMod (
                  customer => {
                    <.tr(
                      <.td(^.className := "text-center",
                        <.i(^.className := "glyphicon glyphicon-search", ^.title := "inspect", ^.onClick --> inspectEO(customer)),
                      ),
                      <.td(customer.name),
                      <.td(customer.dynamicsAccountID),
                      <.td(customer.trigram)
                    )
                  }
                )
              )
            )
        case None => <.div("No customers")
      },
        customer match {
          case Some(cust) =>
            <.div(
              <.h1("Customer: " + customer.get.name).when(customer.isDefined),
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
              )
            )
          case _ => <.div()
        }

      )

    }
  }
  // create the React component for Dashboard
  private val component = ScalaComponent.builder[Props]("AppPage")
    .renderBackend[Backend]
    .componentWillReceiveProps(scope => scope.backend.willReceiveProps(scope.currentProps, scope.nextProps))
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  def apply(ctl: RouterCtl[Loc], proxy: ModelProxy[MegaContent], userId: Option[String], showAddFriends: Boolean) = {
    println("AppPage | apply")
    component(Props(ctl, proxy, userId, showAddFriends))
  }
}
