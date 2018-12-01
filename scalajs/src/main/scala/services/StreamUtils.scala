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


case class StreamForCustomer(trigram: String)
object StreamForCustomer{
  implicit def rw: RW[StreamForCustomer] = macroRW
}


object StreamUtils {
  val baseWebsocketUrl = s"ws://${dom.document.location.host}"


  case class Socket(url: String, trigramOpt: Option[String]) {
    private val socket: WebSocket = new dom.WebSocket(url = baseWebsocketUrl + url)

    def close() = {
      socket.close()
    }

    def connect() {
      socket.onopen = (e: Event) => {
        dom.console.log(s"Socket opened. Reason:")
        trigramOpt match {
          case Some(trigram) =>
            val msg = write(StreamForCustomer(trigram))
            socket.send(msg)
          case None => ()
        }

      }
      socket.onclose = (e: CloseEvent) => {
        dom.console.log(s"Socket closed. Reason: ${e.reason} (${e.code})")
        StreamUtils.createNewEventStream()
      }
      socket.onerror = (e: Event) => {
        dom.console.log(s"Socket error! ${e}")
      }
      /*socket.onmessage = (e: MessageEvent) => {
        println("e.data " + e.data.toString)
      }*/

      socket.onmessage = (e: MessageEvent) => {
        //println("e.data " + e.data.toString)

        val eventType = read[CustomerEventType](e.data.toString)
        val sealedClass = eventType.event_type match {
          case "customerCreated" => "client.CustomerEvent.CustomerCreated"
          case "customerRenamed" => "client.CustomerEvent.CustomerRenamed"
        }
        val sealedType = """"$type":"""" + sealedClass + """","""
        val stringReceived = "{" +sealedType +  e.data.toString.substring(1)

        //println("Socket received completed " + stringReceived)
        val action = read[CustomerEvent](stringReceived)
        //println("Socket received event " + action)

        SPACircuit.dispatch(action)
      }
    }
  }


  // historical events + new events
  def createStream():Socket = {
    val s = Socket("/api/customerEventStream", None)
    s.connect()
    s
  }

  // If we have to reconnect, we are interested only in new events
  // New events
  def createNewEventStream():Socket = {
    val s = Socket("/api/customerNewEventStream", None)
    s.connect()
    s
  }


  def createActivityStream(trigram: String)= {
    val s = Socket("/api/customerEventStream/customer", Some(trigram))
    s.connect()
    s
  }


}
