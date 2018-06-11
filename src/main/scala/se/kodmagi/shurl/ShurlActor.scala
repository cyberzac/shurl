package se.kodmagi.shurl
import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.model.Uri

object ShurlActor {
  final case object GetLongUri
  def props(longUri: Uri): Props = Props(new ShurlActor(longUri))
}

class ShurlActor(longUri: Uri) extends Actor with ActorLogging {
  import se.kodmagi.shurl.ShurlActor.GetLongUri
  def receive: Receive = {
    case GetLongUri =>
      sender() ! Some(longUri)
  }
}
