package com.nagravision.customer.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.nagravision.customer.api.CustomerService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._
import com.lightbend.lagom.scaladsl.pubsub.PubSubComponents

class CustomerLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CustomerApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CustomerApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[CustomerService])
}

abstract class CustomerApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with PubSubComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[CustomerService](wire[CustomerServiceImpl])
  lazy val customerRepository = wire[CustomerRepository]
  lazy val customerService = serviceClient.implement[CustomerService]

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = CustomerSerializerRegistry

  // Register the Hello persistent entity
  persistentEntityRegistry.register(wire[CustomerEntity])
  readSide.register(wire[CustomerEventProcessor])
}
