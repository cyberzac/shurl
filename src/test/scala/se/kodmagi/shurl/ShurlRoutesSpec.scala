package se.kodmagi.shurl
import java.net.URL

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{MessageEntity, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
class ShurlRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with ShurlRoutes {
  override val shurlRegistryActor: ActorRef = system.actorOf(ShurlRegistryActor.props, "shurlRegistry")
  val baseUrl = new URL("http://localhost:8080")
  val routes: Route = shurlRoutes

  "ShurlRoutes" should {
    "create a short url (POST /create) and reply with Created" in {
      val longUrl = LongUrl("http://www.kodmagi.se")
      val shortUrlId = longUrl.shortUrlId
      Post("/create").withEntity(toEntity(longUrl)) ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(s"""{"url":"$baseUrl/${shortUrlId.id}"}""")
      }
    }

    "create a short url (POST /create) and reply with OK if previously created" in {
      val longUrl = LongUrl("http://www.kodmagi.se/index.html")
      val shortUrlId = longUrl.shortUrlId
      Post("/create").withEntity(toEntity(longUrl)) ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(s"""{"url":"$baseUrl/${shortUrlId.id}"}""")
      }
      Post("/create").withEntity(toEntity(longUrl)) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(s"""{"url":"$baseUrl/${shortUrlId.id}"}""")
      }
    }

    "return NotFound (GET /-1)" in {
      Get(uri = "/-1") ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "return Redirect (GET /{existing shortId})" in {
      // create the short url
      val longUrl = LongUrl("https://www.google.com")
      val shortUrlId = longUrl.shortUrlId
      Post("/create").withEntity(toEntity(longUrl)) ~> routes ~> check {
        status should ===(StatusCodes.Created)
      }
      val request = Get(uri = s"/${shortUrlId.id}")
      // Verify the redirect
      request ~> routes ~> check {
        status should ===(StatusCodes.PermanentRedirect)
        header("Location") shouldBe Some(Location(longUrl.url))
        entityAs[String] should ===(s"""The request, and all future requests should be repeated using <a href="${longUrl.url}">this URI</a>.""")
      }
    }
  }
  private def toEntity(url: LongUrl) = {
    Marshal(url).to[MessageEntity].futureValue
  }
}
