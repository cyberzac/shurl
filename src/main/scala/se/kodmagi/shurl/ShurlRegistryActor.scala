package se.kodmagi.shurl
import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.model.{ IllegalUriException, Uri }

object ShurlRegistryActor {
  final case class CreateShortUrl(longUrl: LongUrl)
  sealed trait CreateResult
  final case class CreateResultCreated(shortUrl: ShortUrlId) extends CreateResult
  final case class CreateResultExisting(shortUrl: ShortUrlId) extends CreateResult
  final case class CreateResultError(message: String) extends CreateResult
  final case class GetLongUrl(shortUrl: ShortUrlId)
  def props: Props = Props[ShurlRegistryActor]
}

class ShurlRegistryActor extends Actor with ActorLogging {
  import ShurlRegistryActor._
  def receive: Receive = {
    case CreateShortUrl(longUrl) ⇒
      try {
        val uri = Uri(longUrl.url)
        val shortUrlId = longUrl.shortUrlId
        if (context.child(shortUrlId.id).isEmpty) {
          context.actorOf(ShurlActor.props(uri), shortUrlId.id)
          sender() ! CreateResultCreated(shortUrlId)
        } else {
          sender() ! CreateResultExisting(shortUrlId)
        }
      } catch {
        case e: IllegalUriException ⇒
          log.warning(s"Invalid $longUrl")
          sender() ! CreateResultError(e.info.detail)
      }

    case GetLongUrl(shortUrl) ⇒
      context.child(shortUrl.id) match {
        case Some(ref) ⇒ ref forward ShurlActor.GetLongUri
        case None ⇒ sender() ! None
      }
  }
}