package se.kodmagi.shurl
import java.net.URL

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

import scala.concurrent.duration._

trait ShurlRoutes extends JsonSupport {
  implicit def system: ActorSystem
  lazy val log = Logging(system, classOf[ShurlRoutes])
  def shurlRegistryActor: ActorRef
  implicit val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  def shurlRoutes(baseUrl: URL): Route = concat(
    path("create") {
      post {
        entity(as[LongUrl]) { longUrl =>
          val idCreated = (shurlRegistryActor ? CreateShortUrl(longUrl)).mapTo[ShortUrlId]
          onSuccess(idCreated) { shortUrlId =>
            val shortUrl = shortUrlId.toURL(baseUrl)
            log.info(s"Created short url $longUrl -> $shortUrl")
            complete((StatusCodes.Created, shortUrl))
          }
        }
      }
    },
    path(Segment) { urlId =>
      get {
        val response = (shurlRegistryActor ? GetLongUrl(ShortUrlId(urlId))).mapTo[Option[LongUrl]]
        onSuccess(response) {
          case Some(url) ⇒ redirect(url.uri, StatusCodes.PermanentRedirect)
          case None ⇒ complete(StatusCodes.NotFound, ShortUrlId(urlId))
        }
      }
    })
}
