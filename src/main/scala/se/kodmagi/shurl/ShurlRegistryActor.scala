package se.kodmagi.shurl

import akka.actor.{ Actor, ActorLogging, Props }

object ShurlRegistryActor {

  final case class CreateShortUrl(longUrl: LongUrl)

  final case class GetLongUrl(shortUrl: ShortUrl)

  def props: Props = Props[ShurlRegistryActor]
}

class ShurlRegistryActor extends Actor with ActorLogging {

  import ShurlRegistryActor._

  def receive: Receive = {
    case CreateShortUrl(longUrl) ⇒
      val shortUrl = shortenUrl(longUrl)
      if (context.child(shortUrl.value).isEmpty) {
        context.actorOf(ShurlActor.props(longUrl), shortUrl.value)
      }
      sender() ! shortUrl

    case GetLongUrl(shortUrl) ⇒
      context.child(shortUrl.value) match {
        case Some(ref) ⇒ ref forward ShurlActor.GetLongUrl
        case None ⇒ sender() ! None
      }
  }
}
