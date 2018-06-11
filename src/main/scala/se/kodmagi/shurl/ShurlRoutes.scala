package se.kodmagi.shurl
import java.net.URL

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import se.kodmagi.shurl.ShurlRegistryActor._

import scala.concurrent.duration._

trait ShurlRoutes extends JsonSupport {
  implicit def system: ActorSystem
  implicit val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
  implicit val baseUrl: URL // The external base url, i.e what clients use to contact this server.
  def shurlRegistryActor: ActorRef
  def shurlRoutes: Route = concat(
    path("create") {
      post {
        extractLog {
          implicit log ⇒
            entity(as[LongUrl]) { longUrl =>
              val result = (shurlRegistryActor ? CreateShortUrl(longUrl)).mapTo[CreateResult]
              onSuccess(result) {
                case CreateResultCreated(url) ⇒
                  val shortUrl = url.toURL
                  log.info(s"Created short url $longUrl -> $shortUrl")
                  complete((StatusCodes.Created, shortUrl))
                case CreateResultExisting(url) ⇒
                  val shortUrl = url.toURL
                  complete((StatusCodes.OK, shortUrl))
                case CreateResultError(error) ⇒ complete(StatusCodes.BadRequest, error)
              }
            }
        }
      }
    },
    path(Segment) { urlId =>
      get {
        val response = (shurlRegistryActor ? GetLongUrl(ShortUrlId(urlId))).mapTo[Option[Uri]]
        onSuccess(response) {
          case Some(uri) ⇒ redirect(uri, StatusCodes.PermanentRedirect)
          case None ⇒ complete(StatusCodes.NotFound, ShortUrlId(urlId))
        }
      }
    })
}
