import sbt.Keys._
import sbt.Project.projectToRef

organization in ThisBuild := "com.nagravision"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.12"

val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "4.0.0"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `customer` = (project in file("."))
  .aggregate(`customer-api`, `customer-impl`)


lazy val security = (project in file("security"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
      playJsonDerivedCodecs,
      scalaTest
    )
  )


lazy val `customer-api` = (project in file("customer-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslKafkaBroker,
      playJsonDerivedCodecs
    )
  )
  .dependsOn(security)

lazy val `customer-impl` = (project in file("customer-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`customer-api`)

