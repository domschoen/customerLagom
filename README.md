# customerLagom
Try out of Lagom Framework





# Experience toward having Lagom 1.4.8 and Scala.js and Diode

## publish event in ServiceCall

https://stackoverflow.com/questions/50338216/lagom-publish-entity-events-as-source-for-websocket-connection

## sbt
 - Using Lagom 1.0.0, sbt-less 1.1.0 is not found if ???
## build.sbt:17: error: value playJsonDerivedCodecs is not a member of sbt.ModuleID 

=> missing comma in setting libraryDependencies

## References to undefined settings:  customer-impl/configuration:bundleDiskSpace

=> this plugin cannot be used: addSbtPlugin("com.lightbend.conductr" % "sbt-conductr" % "2.1.7")


## Kafka Server closed unexpectedly

Temporary problem, looks better when other error solved.

Reappear constantly...

After some time this message appears
09:42:09.537 [warn] org.apache.kafka.clients.NetworkClient [] - Connection to node -1 could not be established. Broker may not be available.

=> change scala version from 2.11.11 to 2.11.12

Still existing when running on another machine...

Found a interesting post: https://stackoverflow.com/questions/42675442/lagoms-embedded-kafka-fails-to-start-after-killing-lagom-process-once



## use scala comment in index.scala.html does not work

==> this looks better : <!行 and the comment closes with 行> but if variable inside -> does not work


## (2) [RuntimeException: java.lang.NoSuchMethodError: scalajs.html.jsScript._display_(Ljava/lang/Object;Lscala/reflect/Manifest;)Lplay/twirl/api/Appendable;]

This is a problem related to the use of Scala.js with Play 2.6

This project can be a source of inspiration:
https://github.com/KyleU/boilerplay
=> seems interesting but too clever for me

In Play doc (https://www.playframework.com/documentation/2.6.x/Highlights26), we have a section: Scala.js support explaining that it is supported and how to
=> add this to scalajsDependencies (let's see)
    "com.typesafe.play" %%% "play-json" % "2.6.10",
=> No this is not the reason
=> What is really needed is a change of the version of com.vmunier scalajs-scripts from 1.0.0 to 1.1.2
SOLVED

### vmunier sbt-web-scalajs
com.vmunier
Let's use the latest versions:
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8-0.6")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.23")

=> error: value %%% is not a member of String
(see below)

### sbt new vmunier/play-scalajs.g8
Let's see this example with Scala.js and Play 2.6
Is it working ? yes perfect

Adapt plugins.sbt according to play-scalajs:
Add: addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject"  % "0.5.0")
Change sbt-gzip from 1.0.0 to 1.0.2
Change sbt-digest from 1.1.1 to 1.1.4
Change sbteclipse-plugin from 3.0.0 to 5.2.4

=> problem (1) still here

## (1) value %%% is not a member of String

in sharedDependencies, change "%%%" to "%%" => ok
in scalajsDependencies, why not working ?
=> try changing the scala version from 2.11.11 to 2.12.5
=> still
=> Try changing the sbt.version in build.properties from 0.13.16 to 1.1.2
=> sbt-less;1.1.0: not found
=> try revert to scala 2.11.11
=> change sbt-less from 1.1.0 to 1.1.2
=> change sbt-less from 1.0.0 to 1.3.12
=> still
=> use play-scalajs build.sbt and plugins.sbt
=> Bingo, sbt started !
=> let's revert step by step to previous build.sbt and plugins.sbt
=> ok and problem (2) solved

Conclusion: Removing of Settings.scala solves the problem


## org.apache.kafka.clients.NetworkClient [] - Connection to node -1 could not be established. Broker may not be available

This message is repeated endlessly

## Access localhost:9000 and error Action Not Found

router problem ? Let's look at online-auction-scala-master which also integrate a play app (web-gateway) and is also Lagom 1.4.x

## Referring to non-existent method upickle.default$.write$default$2()scala.Int

try changing sbt-scalajs from 0.6.23 to 0.6.22
=> still


## build.sbt:105: error: No implicit for Append.Values[Seq[org.scalajs.sbtplugin.AbstractJSDep], Seq[sbt.librarymanagement.ModuleID]] found,  so Seq[sbt.librarymanagement.ModuleID] cannot be appended to Seq[org.scalajs.sbtplugin.AbstractJSDep]

## Refused to execute inline script because it violates the following Content Security Policy directive
This works for me: Add to Application.conf:
play.filters.headers.contentSecurityPolicy = null
  
## lagom.discovery.ServiceLocatorServer [] - Ambiguous route resolution serving route
those 2 are overlapping:
  
/api/customer/:trigram  
/api/customer/live

=> changed to /api/customerEventStream

## Websocket handler failed with Peer closed connection with code 1011 '{"name":"Error message truncated","detail":""}'
akka.http.scaladsl.model.ws.PeerClosedConnectionException: Peer closed connection with code 1011 '{"name":"Error message truncated","detail":""}'  

Due to (3)

## (3) Topic#subscribe is not permitted in the service's topic implementation

see https://groups.google.com/forum/#!msg/lagom-framework/K59onuKGYkw/zZiZkTVgAAAJ
(solved)

## Lagom Consumer interrupted with WakeupException after timeout. Message: null

In Chrome: Socket closed. Reason: internal error (1011)
Seems like it is this problem:
https://github.com/lagom/lagom-java-chirper-example/issues/108


????
post a comment on:
https://github.com/lagom/lagom/issues/1221

let's see

Possible references:
https://grokbase.com/t/gg/akka-user/154r8bw5nz/streams-shutting-down-a-flow-exposing-a-materialized-value


May be it is normal !!! and we only need to reconnect in case of error:
https://github.com/lagom/lagom/issues/1284


## only the latest event are send

change this line to set noOffset
      registry.eventStream(tag, Offset.noOffset)

=> try solution: https://stackoverflow.com/questions/48759468/can-lagom-1-4-forward-websocket-error-messages

but now: lagom netty failed: Error during WebSocket handshake: Unexpected response code: 500
 => see (4)
 
 
## (4) Error during WebSocket handshake: Unexpected response code: 500

=> no idea 
=> revert back to akka http
 



## Use PubSub instead of kafka topic to avoid timout and stop of the service

like suggested by:
https://groups.google.com/forum/#!msg/lagom-framework/K59onuKGYkw/zZiZkTVgAAAJ


## Takes 10 second from new event to be visible in websocket

https://github.com/akka/alpakka-kafka/issues/235

Possibly due to: Current value of akka.kafka.consumer.wakeup-timeout is 3000 milliseconds

set fetch min bytes to 0 and increase your poll interval
=> not able to

Seems intricically low but also possible to be reduced

https://discuss.lightbend.com/t/best-practices-for-server-front-end-notifications/610
Solved with same post answer to my question. I was  able to pass from 10s to 0.1s !!

## Why Kafka ?

https://stackoverflow.com/questions/51647937/communication-between-2-lagom-service-running-on-two-different-servers-machines

## org.scalajs.core.tools.linker.LinkingException: There were linking errors

An incompatibility between Play 1.6 and upickle
=> 
 - remove the shared project
 - add "com.lihaoyi" %%% "upickle" % "0.6.6" to client project (was in shared before)

## upickle and sealed trait

When using sealed trait:

Uncaught $c_ju_NoSuchElementException {s$1: "None.get", e$1: null, stackTrace$1: null, stackdata: $c_ju_NoSuchElementException, stack: "Error↵    at $c_ju_NoSuchElementException.fillInSt…:9000/versionedAssets/client-fastopt.js:14200:17)"}
(anonymous) @ Parser.scala:352
(anonymous) @ SyncParser.scala:23
(anonymous) @ StringParser.scala:28
(anonymous) @ StringParser.scala:27
$s_Lupickle_Api$class__read__Lupickle_Api__Lujson_Transformable__Lupickle_core_Types$BaseReader__O @ Transformable.scala:13
(anonymous) @ Api.scala:66

=> the extra case class attribute "$type" was missing

## topic all messages only at startup
 
