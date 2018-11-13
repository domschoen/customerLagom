package loader


import com.lightbend.lagom.scaladsl.api.{LagomConfigComponent, ServiceAcl, ServiceInfo}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.{LagomDevModeComponents, LagomDevModeServiceLocatorComponents}
import com.softwaremill.macwire._
import controllers.{ApplicationController, AssetsComponents}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Mode}
import play.filters.HttpFiltersComponents
import router.Routes

import scala.collection.immutable
import scala.concurrent.ExecutionContext


abstract class MyComponents(context: ApplicationLoader.Context) extends BuiltInComponentsFromContext(context)
  with AssetsComponents
  with HttpFiltersComponents
  with AhcWSComponents
  with LagomConfigComponent
  with LagomServiceClientComponents {

  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
    "customer",
    Map(
      "customer" -> immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
    )
  )
  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher

  override lazy val router = {
    val prefix = "/"
    wire[Routes]
  }
  lazy val appController = wire[ApplicationController]
}

class PlayAppLoader extends ApplicationLoader {
    override def load(context: Context) = context.environment.mode match {
      case Mode.Dev =>
        (new MyComponents(context) with LagomDevModeComponents).application
      case _ =>
        (new MyComponents(context) with LagomDevModeServiceLocatorComponents).application
    }
}
