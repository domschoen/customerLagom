package com.nagravision.customer.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

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


  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def getCustomer(trigram: String): ServiceCall[NotUsed, Customer]

  def getCustomers: ServiceCall[NotUsed, Seq[Customer]]


  override final def descriptor = {
    import Service._
    // @formatter:off
    named("customer")
      .withCalls(
        pathCall("/api/customer", createCustomer),
        pathCall("/api/customer/:trigram", getCustomer _),
        pathCall("/api/customer", getCustomers),
        pathCall("/api/customer/:trigram/rename", renameCustomer _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}
