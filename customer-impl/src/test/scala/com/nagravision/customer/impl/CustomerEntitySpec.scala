package com.nagravision.customer.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class CustomerEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("CustomerEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(CustomerSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[CustomerCommand[_], CustomerEvent, CustomerState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CustomerEntity, "customer-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "Customer entity" should {

    "say hello by default" in withTestDriver { driver =>
      val outcome = driver.run(Hello("Alice"))
      outcome.replies should contain only "Hello, Alice!"
    }

    "allow updating the greeting message" in withTestDriver { driver =>
      val outcome1 = driver.run(UseGreetingMessage("Hi"))
      outcome1.events should contain only GreetingMessageChanged("Hi")
      val outcome2 = driver.run(Hello("Alice"))
      outcome2.replies should contain only "Hi, Alice!"
    }

  }
}
