package com.nagravision.customer.impl

import java.util.UUID

import akka.Done
import com.datastax.driver.core._
import com.nagravision.customer.api.Customer
import com.nagravision.customer.api
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class CustomerRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def getCustomers: Future[Seq[Customer]] = {
    session.selectAll("""
      SELECT * FROM customer
    """).map {
       rows => rows.map(convertRowToCustomer(_))
    }
  }

  private def convertRowToCustomer(row: Row): Customer = {
    Customer(
      row.getString("trigram"),
      row.getString("name"),
      row.getString("customerType"),
      row.getString("dynamicsAccountID"),
      row.getString("headCountry"),
      row.getString("region")
    )
  }
}

private[impl] class CustomerEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[CustomerEvent] {
  private var insertCustomerStatement: PreparedStatement = null
  private var renameCustomerStatement: PreparedStatement = null

  def buildHandler = {
    readSide.builder[CustomerEvent]("customerEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[CustomerCreated](e => insertCustomer(e.event.customer))
      .setEventHandler[CustomerRenamed](e => renameCustomer(e.entityId, e.event.name))
      .build
  }

  def aggregateTags = CustomerEvent.Tag.allTags

  private def createTables() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS customer (
          trigram text PRIMARY KEY,
          name  text,
          customerType text,
          dynamicsAccountID text,
          headCountry text,
          region text
        )
      """)
    } yield Done
  }

  private def prepareStatements() = {
    for {
      insertCustomer <- session.prepare("""
        INSERT INTO customer(
          trigram,
          name,
          customerType,
          dynamicsAccountID,
          headCountry,
          region
        ) VALUES (?, ?, ?, ?, ?, ?)
      """)
      renameCustomer <- session.prepare("""
        UPDATE customer SET name = ? WHERE trigram = ?
      """)
    } yield {
      insertCustomerStatement = insertCustomer
      renameCustomerStatement = renameCustomer
      Done
    }
  }


  private def insertCustomer(customer: Customer) = {
    Future.successful(List(insertCustomerInDB(customer)))
  }
  private def insertCustomerInDB(customer: Customer) = {
    insertCustomerStatement.bind(
      customer.trigram,
      customer.name,
      customer.customerType,
      customer.dynamicsAccountID,
      customer.headCountry,
      customer.region
    )
  }

  private def renameCustomer(trigram: String, name: String) = {
    Future.successful(List(insertCustomerStatement.bind(trigram,name)))
  }

}
