package se.kodmagi.shurl
import akka.actor.{ Actor, ActorLogging, Props }

object ShurlActor {
  final case object GetLongUrl
  def props(longUrl: LongUrl): Props = Props(new ShurlActor(longUrl))
}

class ShurlActor(longUrl: LongUrl) extends Actor with ActorLogging {
  import se.kodmagi.shurl.ShurlActor.GetLongUrl
  def receive: Receive = {
    case GetLongUrl =>
      sender() ! Some(longUrl)
  }
}
