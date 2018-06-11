package se.kodmagi.shurl
import java.net.URL

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object QuickstartServer extends App with ShurlRoutes {
  implicit val system: ActorSystem = ActorSystem("shurlAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val shurlRegistryActor: ActorRef = system.actorOf(ShurlRegistryActor.props, "shurlRegistryActor")
  val port = 8099
  val baseUrl = new URL(s"http://localhost:$port")
  val routes: Route = shurlRoutes
  Http().bindAndHandle(routes, "localhost", port)
  system.log.info(s"Server online at ${baseUrl.toExternalForm}")
  Await.result(system.whenTerminated, Duration.Inf)
}
