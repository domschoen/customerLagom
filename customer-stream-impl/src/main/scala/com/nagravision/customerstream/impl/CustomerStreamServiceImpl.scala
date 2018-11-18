package com.nagravision.customerstream.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.nagravision.customerstream.api.CustomerStreamService
import com.nagravision.customer.api.CustomerService
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

/**
  * Implementation of the HelloStreamService.
  */
class CustomerStreamServiceImpl(customerService: CustomerService) extends CustomerStreamService {
  def stream = ServiceCall { _ =>
    customerService
      .customerEventsTopic
      .subscribe // <-- you get back a Subscriber instance
      .atLeastOnce(
      Flow.fromFunction(_ => {
        println("Stream like a dream : " + _)
        Done
      }
        ))
  }
}
