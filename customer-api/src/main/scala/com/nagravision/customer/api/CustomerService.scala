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


object CustomerService  {
  val TOPIC_NAME = "customer"
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

  def getLiveCustomerEvents: ServiceCall[NotUsed, Source[CustomerEvent, _]]

  def customerEventsTopic: Topic[CustomerEvent]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("customer")
      .withCalls(
        pathCall("/api/customer", createCustomer),
        pathCall("/api/customer/:trigram", getCustomer _),
        pathCall("/api/customer", getCustomers),
        //pathCall("/api/customer/live/:trigram", getLiveCustomerEvents _),
        pathCall("/api/customer/live", getLiveCustomerEvents)(
          MessageSerializer.NotUsedMessageSerializer,
          //MessageSerializer.sourceMessageSerializer(implicitly[MessageSerializer[Source[CustomerEvent,_])
          //MessageSerializer.sourceMessageSerializer(implicitly[MessageSerializer[CustomerEvent, ByteString]]    ))
          streamMessageSerializer
        ),

        //pathCall("/api/customer/live", getLiveCustomerEvents),
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


  def streamMessageSerializer[CustomerEvent] = {
    new StreamedMessageSerializer[JsValue] {

      private class SourceSerializer(delegate: NegotiatedSerializer[JsValue, ByteString]) extends NegotiatedSerializer[Source[JsValue, Any], Source[ByteString, Any]] {
        override def protocol: MessageProtocol = delegate.protocol

        override def serialize(messages: Source[JsValue, Any]) = messages.map(delegate.serialize)
      }

      private class SourceDeserializer(delegate: NegotiatedDeserializer[JsValue, ByteString]) extends NegotiatedDeserializer[Source[JsValue, Any], Source[ByteString, Any]] {
        override def deserialize(wire: Source[ByteString, Any]) = wire.map(delegate.deserialize)
      }

      override def acceptResponseProtocols: immutable.Seq[MessageProtocol] = delegate.acceptResponseProtocols

      override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Source[JsValue, Any], Source[ByteString, Any]] =
        new SourceDeserializer(delegate.deserializer(protocol))

      override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[Source[JsValue, Any], Source[ByteString, Any]] =
        new SourceSerializer(delegate.serializerForResponse(acceptedMessageProtocols))

      override def serializerForRequest: NegotiatedSerializer[Source[JsValue, Any], Source[ByteString, Any]] =
        new SourceSerializer(delegate.serializerForRequest)
    }
  }
}
