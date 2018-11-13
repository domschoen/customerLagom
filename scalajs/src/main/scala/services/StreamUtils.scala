package services

import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom
import org.scalajs.dom.raw.MessageEvent
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}
import shared.StreamForUsers
import upickle.default.write
import upickle.default._
import client.{Chirp, ChirpFromServer, User}
import upickle.default.{macroRW, ReadWriter => RW}

object StreamUtils {
  val baseWebsocketUrl = s"ws://${dom.document.location.host}"


  case class Socket(url: String) {
    private val socket: WebSocket = new dom.WebSocket(url = baseWebsocketUrl + url)

    def close() = {
      socket.close()
    }

    def connect() {
      socket.onopen = (e: Event) => {
        dom.console.log(s"Socket open ${e}")
      }
      socket.onclose = (e: CloseEvent) => {
        dom.console.log(s"Socket closed. Reason: ${e.reason} (${e.code})")
      }
      socket.onerror = (e: Event) => {
        dom.console.log(s"Socket error! ${e}")
      }
      socket.onmessage = (e: MessageEvent) => {
        println("e.data " + e.data)
        val chirp = read[ChirpFromServer](e.data.toString);
        println("Socket received chirp " + chirp)
        SPACircuit.dispatch(ChirpReceived(chirp))
      }
    }
  }



  def createUserStream():Socket = {
    val s = Socket("/api/customer/live")
    s.connect()
    s
  }

  //def createActivityStream(userId: String)= {
  //  Socket("/api/activity/" + userId + "/live", None)
  // }


}
