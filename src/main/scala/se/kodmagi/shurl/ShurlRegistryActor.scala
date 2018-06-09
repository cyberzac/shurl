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
      val shortUrl = toShortUrlId(longUrl)
      if (context.child(shortUrl.id).isEmpty) {
        context.actorOf(ShurlActor.props(longUrl), shortUrl.id)
      }
      sender() ! shortUrl

    case GetLongUrl(shortUrl) ⇒
      context.child(shortUrl.id) match {
        case Some(ref) ⇒ ref forward ShurlActor.GetLongUrl
        case None ⇒ sender() ! None
      }
  }
}
