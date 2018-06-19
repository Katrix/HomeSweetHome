package net.katsstuff.homesweethome

import java.nio.file.Path

import io.circe._
import io.circe.generic.semiauto

case class HomeConfig(
    homeLimitDefault: Int,
    residentLimitDefault: Int,
    timeout: Int
)
object HomeConfig {
  implicit val encoder: Encoder[HomeConfig] = semiauto.deriveEncoder
  implicit val decoder: Decoder[HomeConfig] = semiauto.deriveDecoder
}
trait HomeConfigLoader[F[_]] {

  def load(path: Path): F[HomeConfig]
}