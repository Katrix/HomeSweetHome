package net.katsstuff.homesweethome

import java.nio.file.Path

import cats.effect.IO
import cats.syntax.either._
import metaconfig._
import metaconfig.annotation.Description
import metaconfig.generic.Surface
import metaconfig.typesafeconfig.TypesafeConfig2Class

case class HomeConfig(
    @Description("Type = Int\nThe default limit to how many homes someone can have") homeLimitDefault: Int,
    @Description("Type = Int\nThe default limit to how many residents a home can have") residentLimitDefault: Int,
    @Description("Type = Int\nThe amount of time in seconds before an invite or request times out") timeout: Int
)
object HomeConfig {
  implicit val surface: Surface[HomeConfig]     = generic.deriveSurface[HomeConfig]
  implicit val decoder: ConfDecoder[HomeConfig] = generic.deriveDecoder(HomeConfig(3, 2, 60 * 5))

  def load(file: Path): IO[HomeConfig] = {
    IO {
      TypesafeConfig2Class.gimmeConfFromFile(file.toFile).andThen(_.as[HomeConfig])
    }.flatMap(conf => IO.fromEither(conf.toEither.leftMap(e => e.cause.getOrElse(new Exception(e.msg)))))
  }
}
