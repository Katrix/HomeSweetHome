package net.katsstuff.homesweethome

import java.nio.file.{Files, Path}

import org.spongepowered.api.Sponge

import cats.effect.IO
import cats.syntax.either._
import metaconfig._
import metaconfig.annotation.Description
import metaconfig.generic.Surface
import metaconfig.typesafeconfig.TypesafeConfig2Class
import net.katsstuff.katlib.helper.Implicits._
import net.katsstuff.katlib.helper.LogHelper

case class HomeConfig(
    @Description("Type = Int\nThe default limit to how many homes someone can have") homeLimitDefault: Int,
    @Description("Type = Int\nThe default limit to how many residents a home can have") residentLimitDefault: Int,
    @Description("Type = Int\nThe amount of time in seconds before an invite or request times out") timeout: Int
)
object HomeConfig {
  implicit val surface: Surface[HomeConfig]     = generic.deriveSurface[HomeConfig]
  implicit val decoder: ConfDecoder[HomeConfig] = generic.deriveDecoder(HomeConfig(3, 2, 60 * 5))

  def createAndReadConfig(file: Path)(implicit plugin: HomePlugin): IO[Configured[Conf]] = {
    for {
      optAsset <- IO(Sponge.getAssetManager.getAsset(plugin, "reference.conf").toOption)
      conf <- optAsset match {
        case Some(asset) =>
          for {
            _    <- IO(asset.copyToFile(file))
            conf <- IO(TypesafeConfig2Class.gimmeConfFromFile(file.toFile))
          } yield conf
        case None => IO.pure(Configured.notOk(ConfError.fileDoesNotExist(file)))
      }
    } yield conf
  }

  def load(file: Path)(implicit plugin: HomePlugin): IO[HomeConfig] =
    for {
      _            <- IO(LogHelper.info("Loading Config"))
      _            <- IO(Files.createDirectories(file.getParent))
      fileNotExist <- IO(Files.notExists(file))
      configured <- if (fileNotExist) createAndReadConfig(file)
      else IO(TypesafeConfig2Class.gimmeConfFromFile(file.toFile))
      configuredConfig = configured.andThen(_.as[HomeConfig])
      config <- IO.fromEither(configuredConfig.toEither.leftMap(e => e.cause.getOrElse(new Exception(e.msg))))
    } yield config
}
