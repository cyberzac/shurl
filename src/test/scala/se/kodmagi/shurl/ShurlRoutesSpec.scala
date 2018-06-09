package se.kodmagi.shurl

import java.net.URL

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class ShurlRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with ShurlRoutes {
  override val shurlRegistryActor: ActorRef = system.actorOf(ShurlRegistryActor.props, "shurlRegistry")

  val baseUrl = new URL("http://localhost:8080")
  lazy val routes = shurlRoutes(baseUrl)
  val longUrl = LongUrl("http://www.kodmagi.se")
  val shortUrlId = toShortUrlId(longUrl)

  "ShurlRoutes" should {
    "Create a short url (POST /create)" in {
      val request = postLongUrlRequest(longUrl)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(s"""{"url":"$baseUrl/${shortUrlId.id}"}""")
      }
    }

    "return NotFound (GET /-1)" in {
      val request = HttpRequest(uri = "/-1")

      request ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "return Redirect (GET /{existing shortId})" in {
      // create the ur
      postLongUrlRequest(longUrl) ~> routes

      val request = HttpRequest(uri = s"/$shortUrlId")

      request ~> routes ~> check {
        status should ===(StatusCodes.Redirection)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"users":[]}""")
      }
    }
  }

  private def postLongUrlRequest(longUrl: LongUrl) = {
    val longEntity = Marshal(longUrl).to[MessageEntity].futureValue // futureValue is from ScalaFutures
    Post("/create").withEntity(longEntity)
  }
}
