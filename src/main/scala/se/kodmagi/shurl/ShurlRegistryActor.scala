package se.kodmagi.shurl
import akka.actor.{ Actor, ActorLogging, Props }

object ShurlRegistryActor {
  final case class CreateShortUrl(longUrl: LongUrl)
  final case class GetLongUrl(shortUrl: ShortUrlId)
  def props: Props = Props[ShurlRegistryActor]
}

class ShurlRegistryActor extends Actor with ActorLogging {
  import ShurlRegistryActor._
  def receive: Receive = {
    case CreateShortUrl(longUrl) ⇒
      val shortUrlId = longUrl.shortUrlId
      if (context.child(shortUrlId.id).isEmpty) {
        context.actorOf(ShurlActor.props(longUrl), shortUrlId.id)
      }
      sender() ! shortUrlId

    case GetLongUrl(shortUrl) ⇒
      context.child(shortUrl.id) match {
        case Some(ref) ⇒ ref forward ShurlActor.GetLongUrl
        case None ⇒ sender() ! None
      }
  }
}