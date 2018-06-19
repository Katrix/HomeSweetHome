package net.katsstuff.homesweethome.command

import scala.concurrent.ExecutionContext.Implicits.global

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.format.TextColors._

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.all._
import net.katsstuff.katlib.helper.Implicits._
import net.katsstuff.katlib.i18n.Localized
import net.katsstuff.homesweethome.lib.LibPerm
import net.katsstuff.homesweethome.{HomeConfig, HomePlugin, Storage}
import net.katsstuff.scammander.sponge.CommandInfo

object BaseCmds {
  def HomeSweetHomeCmd(implicit plugin: HomePlugin): SpongeCommandWrapper[CommandSource, NotUsed] =
    Command
      .withChildren[NotUsed](Set(ReloadCmd)) { (sender, _, _) =>
        sender.sendMessage(t"$YELLOW${plugin.container.name} ${plugin.container.version}")
        Command.successStep()
      }
      .toSponge(CommandInfo(shortDescription = Description(t"Shows info about HomeSweetHome")))

  def ReloadCmd(implicit plugin: HomePlugin): ChildCommand[CommandSource, NotUsed] =
    Command
      .simple[NotUsed] { (sender, _, _) =>
        Localized(sender) { implicit locale =>
          def loadData[A](load: IO[A], name: String) =
            (IO.shift *> load).attemptT
              .leftMap(e => IO(e.printStackTrace()).as(Command.error(s"Couldn't load $name ${e.getMessage}")))
              .leftSemiflatMap(identity)
              .value

          val loadConfig  = loadData(HomeConfig.load(plugin.configPath), "config")
          val loadStorage = loadData(Storage.load(plugin.configPath), "storage")

          val reload = EitherT {
            (loadConfig, loadStorage)
              .parMapN {
                case (configOpt, storageOpt) =>
                  for {
                    config  <- configOpt
                    storage <- storageOpt
                  } yield
                    for {
                      _ <- IO(homeHandler.reloadHomeData(storage, config))
                      _ <- IO(sender.sendMessage(t"${GREEN}Reload success"))
                    } yield Command.success()
              }
          }.semiflatMap(identity).value

          reload.unsafeRunSync()
        }
      }
      .toChild(Alias("reload"), Permission(LibPerm.Reload), shortDescription = Description(t"Reloads stuff"))
}
