package com.nagravision.customerstream.api

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

/**
  * The Customer stream interface.
  */
trait CustomerStreamService extends Service {

  def stream: ServiceCall[NotUsed, Done]

  override final def descriptor = {
    import Service._

    named("customer-stream")
      .withCalls(
        namedCall("/api/stream/customer", stream )
      ).withAutoAcl(true)
  }
}

