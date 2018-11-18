package services

import upickle.default.read

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import upickle.default._
import client.{Customer}
import upickle.default.{macroRW, ReadWriter => RW}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax


object CustomerUtils {

  /*def getCustomers() : Future[Option[List[Customer]]] = {
    Future(None)
  }*/
  def getCustomers() : Future[Option[List[Customer]]] = {
    Ajax.get("/api/customer").recover {
      // Recover from a failed error code into a successful future
      case dom.ext.AjaxException(req) => req
    }.map( r =>
      r.status match {
        case 200 =>
          val customers = read[List[Customer]](r.responseText)
          Some(customers)
        case _ =>
          None
      }
    )
  }

}
