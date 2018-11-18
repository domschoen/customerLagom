package controllers

import akka.stream.scaladsl.Flow
import akka.util.ByteString
import javax.inject._
import play.api.http.websocket.{BinaryMessage, CloseCodes, CloseMessage, Message}
import play.api.libs.streams.{ActorFlow, AkkaStreams}
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def index = Action {
    Ok(views.html.index("Customer Repo"))
  }

}
