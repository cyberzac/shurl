package se.kodmagi.shurl

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import se.kodmagi.shurl.ShurlRegistryActor.{ CreateShortUrl, GetLongUrl }

import scala.concurrent.Future
import scala.concurrent.duration._

trait ShurlRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[ShurlRoutes])

  def shurlRegistryActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val shurlRoutes: Route = concat(
    pathPrefix("create") {
      pathEnd {
        post {
          entity(as[LongUrl]) { longUrl =>
            val urlCreated: Future[ShortUrl] =
              (shurlRegistryActor ? CreateShortUrl(longUrl)).mapTo[ShortUrl]
            onSuccess(urlCreated) { shortUrl =>
              log.info(s"Created short url $shortUrl -> $longUrl")
              complete((StatusCodes.Created, shortUrl))
            }
          }
        }
      }
    },
    path(Segment) { name =>
      concat(
        get {
          val maybeUrl: Future[Option[LongUrl]] =
            (shurlRegistryActor ? GetLongUrl(ShortUrl(name))).mapTo[Option[LongUrl]]
          rejectEmptyResponse {
            complete(maybeUrl)
          }
        })
    })
}
