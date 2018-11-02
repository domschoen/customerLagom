package com.nagravision.customer.api


import java.time.{Duration, Instant}
import java.util.UUID

import com.lightbend.lagom.scaladsl.api.deser.PathParamSerializer
import play.api.libs.json.{Format, Json}


case class CustomerNewName(name: String)

object CustomerNewName {
  implicit val format: Format[CustomerNewName] = Json.format
}

case class Customer (
  trigram: Option[String],
  name: String,
  customerType: String,
  dynamicsAccountID: String,
  headCountry: String,
  region: String
)


object Customer {
  implicit val format: Format[Customer] = Json.format

  def create(
              trigram: Option[String],
              name: String,
              customerType: String,
              dynamicsAccountID: String,
              headCountry: String,
              region: String
            ) = Customer(trigram, name, customerType,dynamicsAccountID, headCountry, region)

}
