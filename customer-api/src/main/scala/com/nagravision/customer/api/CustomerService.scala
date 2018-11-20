package com.nagravision.customer.api

import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, JsValue, Json}
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.deser.{MessageSerializer, StreamedMessageSerializer}
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import com.nagravision.customer.utils.JsonFormats._


object CustomerService  {
  val TOPIC_NAME = "customerTopic"
}



/**
  * The Customer service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the CustomerService.
  */
trait CustomerService extends Service {

  /**
    * Create a customer.
    *
    * @return The created customer.
    */
  def createCustomer: ServiceCall[Customer, Done]

  def renameCustomer(trigram: String): ServiceCall[CustomerNewName, Done]


  def getCustomer(trigram: String): ServiceCall[NotUsed, Customer]
  //def getCustomerEvents(trigram: String): ServiceCall[NotUsed,  Seq[CustomerEvent]]
  def getCustomers: ServiceCall[NotUsed, Seq[Customer]]

  def getLiveAllCustomerEvents(): ServiceCall[NotUsed, Source[CustomerEvent, NotUsed]]
  def getLiveCustomerEvents(): ServiceCall[LiveCustomerEventsRequest, Source[CustomerEvent, NotUsed]]

  def customerEventsTopic(): Topic[CustomerEvent]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("customer-serv")
      .withCalls(
        pathCall("/api/customer", createCustomer),
        pathCall("/api/customer/:trigram", getCustomer _),
        pathCall("/api/customer", getCustomers),

        pathCall("/api/customerEventStream", getLiveAllCustomerEvents),
        pathCall("/api/customerEventStream/customer", getLiveCustomerEvents _),
        pathCall("/api/customer/:trigram/rename", renameCustomer _)
      )
      .withTopics(
        topic(CustomerService.TOPIC_NAME, customerEventsTopic)
           .addProperty(
                  KafkaProperties.partitionKeyStrategy,
                  PartitionKeyStrategy[CustomerEvent](_.trigram)
           )
      )
      .withAutoAcl(true)
    // @formatter:on
  }

}

case class LiveCustomerEventsRequest(trigram: String)


object LiveCustomerEventsRequest {
  implicit val format: Format[LiveCustomerEventsRequest] = Json.format
}

