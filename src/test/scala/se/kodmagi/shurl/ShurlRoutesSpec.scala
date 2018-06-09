package se.kodmagi.shurl
import java.net.URL

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ MessageEntity, _ }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
class ShurlRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with ShurlRoutes {
  override val shurlRegistryActor: ActorRef = system.actorOf(ShurlRegistryActor.props, "shurlRegistry")
  val baseUrl = new URL("http://localhost:8080")
  lazy val routes = shurlRoutes(baseUrl)
  val longUrl = LongUrl("http://www.kodmagi.se")
  val longEntity = Marshal(longUrl).to[MessageEntity].futureValue // futureValue is from ScalaFutures
  val shortUrlId = longUrl.shortUrlId

  "ShurlRoutes" should {
    "create a short url (POST /create)" in {
      Post("/create").withEntity(longEntity) ~> routes ~> check {
        status should ===(StatusCodes.Created)
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
      Post("/create").withEntity(longEntity) ~> routes ~> check {
        status should ===(StatusCodes.Created)
      }
      val request = Get(uri = s"/${shortUrlId.id}")
      // Verify the redirect
      request ~> routes ~> check {
        status should ===(StatusCodes.PermanentRedirect)
        entityAs[String] should ===("The request, and all future requests should be repeated using <a href=\"http://www.kodmagi.se\">this URI</a>.")
      }
    }
  }
}
