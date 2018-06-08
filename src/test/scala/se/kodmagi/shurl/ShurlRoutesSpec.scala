package se.kodmagi.shurl

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class ShurlRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with ShurlRoutes {
  override val shurlRegistryActor: ActorRef = system.actorOf(ShurlRegistryActor.props, "shurlRegistry")

  lazy val routes = shurlRoutes

  "ShurlRoutes" should {
    //    "return no users if no present (GET /users)" in {
    //      val request = HttpRequest(uri = "/users")
    //
    //      request ~> routes ~> check {
    //        status should ===(StatusCodes.OK)
    //
    //        // we expect the response to be json:
    //        contentType should ===(ContentTypes.`application/json`)
    //
    //        // and no entries should be in the list:
    //        entityAs[String] should ===("""{"users":[]}""")
    //      }
    //    }
    "be able create a short url (POST /create)" in {
      val shortUrl = ShortUrl("http://www.kodmagi.se")
      val shortEntity = Marshal(shortUrl).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/create").withEntity(shortEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"User Kapi created."}""")
      }
    }

  }
}
