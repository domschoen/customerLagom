// repository for Typesafe plugins
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.vmunier"               % "sbt-web-scalajs"           % "1.0.8-0.6")

addSbtPlugin("org.scala-js"              % "sbt-scalajs"               % "0.6.23")

addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject"  % "0.5.0")

addSbtPlugin("com.typesafe.sbt"          % "sbt-gzip"                  % "1.0.2")

addSbtPlugin("com.typesafe.sbt"          % "sbt-digest"                % "1.1.4")

addSbtPlugin("com.typesafe.sbteclipse"   % "sbteclipse-plugin"         % "5.2.4")


addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.4.9")

