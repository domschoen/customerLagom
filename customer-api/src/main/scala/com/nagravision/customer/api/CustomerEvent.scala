package com.nagravision.customer.api


import julienrf.json.derived
import play.api.libs.json._


sealed trait CustomerEvent {
  val trigram: String
}


case class CustomerCreated(trigram: String, name: String, customerType: String, dynamicsAccountID: String, headCountry: String, region: String) extends CustomerEvent
case class CustomerRenamed(trigram: String, name: String) extends CustomerEvent


object ItemEvent {
  implicit val format: Format[CustomerEvent] =
    derived.flat.oformat((__ \ "type").format[String])
}
