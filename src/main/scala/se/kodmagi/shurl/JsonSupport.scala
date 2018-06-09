package se.kodmagi.shurl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  import spray.json._
  import DefaultJsonProtocol._

  implicit val longUrlJsonFormat = jsonFormat1(LongUrl)
  implicit val shortUrlJsonFormat = jsonFormat1(ShortUrl)
  implicit val shortUrlIdJsonFormat = jsonFormat1(ShortUrlId)
}
