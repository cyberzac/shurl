package se.kodmagi

import java.net.URL

import akka.http.scaladsl.model.Uri

/**
 *
 */
package object shurl {

  final case class LongUrl(url: String) {
    def uri: Uri = Uri(url)
  }

  final case class ShortUrlId(id: String) {
    def toURL(baseURL: URL): ShortUrl = ShortUrl(s"${baseURL.toExternalForm}/$id")
  }

  final case class ShortUrl(url: String)

  def toShortUrlId(longUrl: LongUrl): ShortUrlId = ShortUrlId(longUrl.url.hashCode.toHexString) // Todo make nicer

}
