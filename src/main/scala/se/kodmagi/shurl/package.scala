package se.kodmagi

/**
 *
 */
package object shurl {

  final case class LongUrl(value: String)

  final case class ShortUrl(value: String)

  def shortenUrl(longUrl: LongUrl): ShortUrl = ShortUrl(longUrl.hashCode.toHexString) // Todo make nicer
}
