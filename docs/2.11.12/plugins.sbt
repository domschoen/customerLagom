// repository for Typesafe plugins
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.22")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")


addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.0")


addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6")

//addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.0-M1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")



// The Lagom plugin
//addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.3.10")
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.4.8")
// Needed for importing the project into Eclipse

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.0")

