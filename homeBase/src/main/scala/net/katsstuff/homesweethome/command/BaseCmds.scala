package net.katsstuff.homesweethome.command

import cats.effect.Sync
import cats.effect.concurrent.MVar
import cats.kernel.Monoid
import cats.syntax.all._
import cats.~>
import net.katsstuff.homesweethome.home.HomeHandler
import net.katsstuff.homesweethome.lib.LibPerm
import net.katsstuff.homesweethome.{HomeConfig, HomeConfigLoader, HomeGlobal, Storage}
import net.katsstuff.minejson.text._
import net.katstuff.katlib.algebras.{CommandSourceAccess, Localized, Pagination, PlayerAccess, Resource, Users}
import net.katstuff.katlib.syntax._

abstract class BaseCmds[F[_], G[_], Page: Monoid, CommandSource, Player, User, Location](
    FtoG: F ~> G,
    configVar: MVar[F, HomeConfig]
)(
    implicit
    realF: Sync[F],
    storage: Storage[F],
    configLoader: HomeConfigLoader[F],
    global: HomeGlobal[F],
    pagination: Pagination.Aux[F, CommandSource, Page],
    LocalizedF: Localized[F, CommandSource],
    homeHandler: HomeHandler[F, Player, User, Location],
    players: PlayerAccess[F, Player, User],
    users: Users[F, User, Player],
    commandSources: CommandSourceAccess[F, CommandSource],
    resource: Resource[F]
) extends HomeCommandBundle[F, G, Page, CommandSource, Player, User, Location](FtoG) {

  def HomeSweetHomeCmd: ChildCommand =
    Command
      .withChildren[NotUsed](Set(ReloadCmd)) { (sender, _, _) =>
        sender.sendMessage(t"$Yellow${global.name} ${global.version}")
        Command.successF()
      }
      .toChild(KAlias("homesweethom"), description = LocalizedDescription("cmd.homesweethome.description"))

  def ReloadCmd: ChildCommand =
    Command
      .simple[NotUsed] { (sender, _, _) =>
        LocalizedG(sender) { implicit locale =>
          def loadData[A](load: F[A], name: String) =
            FtoG(
              (global.shiftAsync *> load).attemptT
                .leftMap(
                  e => realF.delay(e.printStackTrace()).as(Command.errorNel(s"Couldn't load $name ${e.getMessage}"))
                )
                .leftSemiflatMap(identity)
                .value
            ).flatMap(F.fromEither(_))

          val loadConfig  = loadData(configLoader.load(global.configPath), "config")
          val loadStorage = loadData(storage.load(global.storagePath), "storage")

          (loadConfig, loadStorage)
            .parMapN {
              case (config, homeData) =>
                for {
                  _ <- configVar.put(config)
                  _ <- homeHandler.reloadHomeData(homeData)
                  _ <- sender.sendMessage[F](t"${Green}Reload success")
                } yield Command.success()
            }
            .flatMap(FtoG(_))
        }
      }
      .toChild(
        KAlias("reload"),
        KPermission(LibPerm.Reload),
        description = LocalizedDescription("cmd.reload.description")
      )
}
