package se.kodmagi.shurl

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object QuickstartServer extends App with ShurlRoutes {
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val shurlRegistryActor: ActorRef = system.actorOf(ShurlRegistryActor.props, "shurlRegistryActor")

  lazy val routes: Route = shurlRoutes
  Http().bindAndHandle(routes, "localhost", 8099)

  println(s"Server online at http://localhost:8099/")

  Await.result(system.whenTerminated, Duration.Inf)
}
