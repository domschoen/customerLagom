import sbtcrossproject.{crossProject, CrossType}
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
  .aggregate(`customer-api`, `customer-impl`, `server`)


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



lazy val server = (project in file("server")).settings(commonSettings).settings(
  scalaJSProjects := Seq(client),
  routesGenerator := InjectedRoutesGenerator,
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % Test,
    "org.webjars" % "font-awesome" % "4.3.0-1" % Provided,
    "org.webjars" % "bootstrap" % "3.3.6" % Provided,
    "com.esotericsoftware.kryo" % "kryo" % "2.24.0",
    "com.lihaoyi" %% "utest" % "0.4.7" % Test,
    lagomScaladslServer,
    macwire,
    guice,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala, LagomPlay).
  dependsOn(sharedJvm)



lazy val client = (project in file("scalajs")).settings(commonSettings).settings(
  name := "client",
  scalaJSUseMainModuleInitializer := true,
  scalacOptions ++= Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  ),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0",
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.1.0",
    "com.github.japgolly.scalacss" %%% "ext-react" % "0.5.3",
    "com.typesafe.play" %%% "play-json" % "2.6.10",
    "io.suzaku" %%% "diode" % "1.1.3",
    "io.suzaku" %%% "diode-react" % "1.1.3",
    "com.zoepepper" %%% "scalajs-jsjoda" % "1.1.1",
    "com.lihaoyi" %%% "utest" % "0.4.7" % Test
  ),
  jsDependencies ++= Seq(
    "org.webjars.bower" % "react" % "15.6.1" / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
    "org.webjars.bower" % "react" % "15.6.1" / "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM",
    "org.webjars" % "jquery" % "1.11.1" / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "bootstrap" % "3.3.6" / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js",
    "org.webjars" % "log4javascript" % "1.4.10" / "js/log4javascript_uncompressed.js" minified "js/log4javascript.js",
    "org.webjars.npm" % "js-joda" % "1.1.8" / "dist/js-joda.js" minified "dist/js-joda.min.js"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)


lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings).settings(
      libraryDependencies ++= Seq(
          "com.lihaoyi" %% "upickle" % "0.6.6"
      )
  )
  
  
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.11.11"
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}
