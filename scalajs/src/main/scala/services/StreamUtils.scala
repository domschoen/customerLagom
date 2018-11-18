package services


import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom


import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom
import org.scalajs.dom.raw.MessageEvent
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}
import upickle.default.write
import upickle.default._
import upickle.default.{macroRW, ReadWriter => RW}
import client.{CustomerEvent, CustomerCreated, CustomerRenamed,CustomerEventType}
import diode.Action
import diode.react.ModelProxy

object StreamUtils {
  val baseWebsocketUrl = s"ws://${dom.document.location.host}"


  case class Socket(url: String) {
    private val socket: WebSocket = new dom.WebSocket(url = baseWebsocketUrl + url)

    def close() = {
      socket.close()
    }

    def connect() {
      socket.onopen = (e: Event) => {
        dom.console.log(s"Socket opened. Reason:")
      }
      socket.onclose = (e: CloseEvent) => {
        dom.console.log(s"Socket closed. Reason: ${e.reason} (${e.code})")
        StreamUtils.createStream()
      }
      socket.onerror = (e: Event) => {
        dom.console.log(s"Socket error! ${e}")
      }
      socket.onmessage = (e: MessageEvent) => {
        println("e.data " + e.data.toString)
        val eventType = read[CustomerEventType](e.data.toString);
        println("Socket received event " + eventType)
        val action : Action = eventType.event_type match {
          case "customerCreated" =>
            val customerEvent = read[CustomerCreated](e.data.toString)
            CustomerCreatedReceived(customerEvent)
          case "customerRenamed" =>
            val customerEvent = read[CustomerRenamed](e.data.toString)
            CustomerRenamedReceived(customerEvent)
        }
        SPACircuit.dispatch(action)
      }
    }
  }



  def createStream():Socket = {
    val s = Socket("/api/customerEventStream")
    s.connect()
    s
  }

  //def createActivityStream(userId: String)= {
  //  Socket("/api/activity/" + userId + "/live", None)
  // }


}
