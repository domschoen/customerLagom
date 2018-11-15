import com.lightbend.lagom.scaladsl.api.{LagomConfigComponent, ServiceAcl, ServiceInfo}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.softwaremill.macwire._
//import com.lightbend.rp.servicediscovery.lagom.scaladsl.LagomServiceLocatorComponents
import controllers.{Assets, AssetsComponents}
import com.example.playscalajs.controllers.Main
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Mode}
import play.filters.HttpFiltersComponents
import play.i18n.I18nComponents
import router.Routes

import scala.collection.immutable
import scala.concurrent.ExecutionContext

abstract class CustomerRep(context: Context) extends BuiltInComponentsFromContext(context)
  with AssetsComponents
  with HttpFiltersComponents
  with AhcWSComponents
  with LagomConfigComponent
  with LagomServiceClientComponents {

  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
    "web-gateway",
    Map(
      "web-gateway" -> immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
    )
  )
  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher

  override lazy val router = {
    val prefix = "/"
    wire[Routes]
  }

  lazy val main = wire[Main]
}

class CustomerRepLoader extends ApplicationLoader {
  override def load(context: Context) = context.environment.mode match {
    case Mode.Dev =>
      (new CustomerRep(context) with LagomDevModeComponents).application
    case _ =>
      (new CustomerRep(context) with LagomDevModeComponents).application
  }
}
