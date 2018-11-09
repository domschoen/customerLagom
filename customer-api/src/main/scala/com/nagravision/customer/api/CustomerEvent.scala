package com.nagravision.customer.api

//import julienrf.json.derived
import play.api.libs.json._

import scala.collection.immutable.Seq
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}
import com.nagravision.customer.utils.JsonFormats._





sealed trait CustomerEvent {
  val trigram: String
}
/*object CustomerEvent {
  implicit val format: Format[CustomerEvent] =
    derived.flat.oformat((__ \ "type").format[String])
}*/

object CustomerEvent {
  implicit val reads: Reads[CustomerEvent] = {
    (__ \ "event_type").read[String].flatMap {
      case "customerCreated" => implicitly[Reads[CustomerCreated]].map(identity)
      case "customerRenamed" => implicitly[Reads[CustomerRenamed]].map(identity)
      case other => Reads(_ => JsError(s"Unknown event type $other"))
    }
  }
  implicit val writes: Writes[CustomerEvent] = Writes { event =>
    val (jsValue, eventType) = event match {
      case m: CustomerCreated => (Json.toJson(m)(CustomerCreated.format), "customerCreated")
      case m: CustomerRenamed => (Json.toJson(m)(CustomerRenamed.format), "customerRenamed")
    }
    jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
  }
}


case class CustomerCreated(trigram: String, name: String, customerType: String, dynamicsAccountID: String, headCountry: String, region: String) extends CustomerEvent
object CustomerCreated {
  implicit val format: Format[CustomerCreated] = Json.format
}

case class CustomerRenamed(trigram: String, name: String) extends CustomerEvent
object CustomerRenamed {
  implicit val format: Format[CustomerRenamed] = Json.format
}


object CustomerSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[CustomerCreated],
    JsonSerializer[CustomerRenamed]
  )
}


