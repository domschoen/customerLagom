package com.nagravision.customerstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.nagravision.customerstream.api.CustomerStreamService
import com.nagravision.customer.api.CustomerService
import com.softwaremill.macwire._

class CustomerStreamLoader extends LagomApplicationLoader  {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CustomerStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CustomerStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[CustomerStreamService])
}

abstract class CustomerStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents
    with LagomKafkaClientComponents
{

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[CustomerStreamService](wire[CustomerStreamServiceImpl])

  // Bind the CustomerService client
  lazy val customerService = serviceClient.implement[CustomerService]
}
