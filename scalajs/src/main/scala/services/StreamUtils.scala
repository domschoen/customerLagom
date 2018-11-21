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
import client.{Customer, CustomerEventType, CustomerEvent}
import client.CustomerEvent.{CustomerRenamed, CustomerCreated}
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
        //StreamUtils.createStream()
      }
      socket.onerror = (e: Event) => {
        dom.console.log(s"Socket error! ${e}")
      }
      /*socket.onmessage = (e: MessageEvent) => {
        println("e.data " + e.data.toString)
      }*/

      socket.onmessage = (e: MessageEvent) => {
        println("e.data " + e.data.toString)

        val eventType = read[CustomerEventType](e.data.toString)
        val sealedClass = eventType.event_type match {
          case "customerCreated" => "client.CustomerEvent.CustomerCreated"
          case "customerRenamed" => "client.CustomerEvent.CustomerRenamed"
        }
        val sealedType = """"$type":"""" + sealedClass + """","""
        val stringReceived = "{" +sealedType +  e.data.toString.substring(1)

        println("Socket received completed " + stringReceived)
        val action = read[CustomerEvent](stringReceived)
        println("Socket received event " + action)

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
