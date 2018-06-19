package net.katsstuff.homesweethome.command

import cats.data.StateT
import cats.{FlatMap, ~>}
import cats.kernel.Monoid
import cats.syntax.all._
import net.katsstuff.homesweethome.home.HomeHandler
import net.katsstuff.homesweethome.lib.LibPerm
import net.katsstuff.scammander.RawCmdArg
import net.katsstuff.minejson.text._
import net.katstuff.katlib.algebras.{CommandSourceAccess, Localized, Pagination, PlayerAccess, Resource, Users}
import net.katstuff.katlib.syntax._
import shapeless.{Witness => W}

abstract class HomeCmds[F[_]: FlatMap, G[_], Page: Monoid, CommandSource, Player, User, Location](
    FtoG: F ~> G,
    baseCommands: BaseCmds[F, G, Page, CommandSource, Player, User, Location]
)(
    implicit
    pagination: Pagination.Aux[F, CommandSource, Page],
    LocalizedF: Localized[F, CommandSource],
    LocalizedG: Localized[G, CommandSource],
    LocalizedSF: Localized[StateT[G, List[RawCmdArg], ?], CommandSource],
    homeHandler: HomeHandler[F, Player, User, Location],
    players: PlayerAccess[F, Player, User],
    users: Users[F, User, Player],
    commandSources: CommandSourceAccess[F, CommandSource],
    resource: Resource[F]
) extends HomeCommandBundle[F, G, Page, CommandSource, Player, User, Location](FtoG) {

  def homeCmdChildren: Set[ChildCommand] = Set(
    HomeSetCmd,
    HomeDeleteCmd,
    HomeListCmd.toChild(KAlias("list"), description = LocalizedDescription("cmd.list.description")),
    HomeLimitCmd,
    HomeAcceptCmd,
    HomeGotoCmd,
    HomeInviteCmd,
    ResidentsCmds.HomeResidentsCmd,
    HomeHelpCmd
  )

  def HomeHelpCmd: ChildCommand =
    helpCommand(
      t"HomeSweetHome commands",
      Set(
        HomeCmd,
        baseCommands.HomeSweetHomeCmd,
        HomeListCmd.toChild(KAlias("homes"), description = LocalizedDescription("cmd.list.description"))
      )
    ).toChild(KAlias("help"), description = LocalizedDescription("cmd.help.description"))

  //TODO: Support no args here use Residents args code
  def HomeCmd: ChildCommand =
    Command
      .withSenderAndChildren[Player, OtherHomeArgs[NotUsed]](homeCmdChildren) {
        case (player, _, args) =>
          LocalizedG(player) { implicit locale =>
            F.ifM(args.home.teleport(player))(
              FtoG(players.sendMessage(player, t"$Green${args.messageWithHomeName("cmd.home.success")}")) *> Command
                .successF(),
              teleportError
            )
          }
      }
      .toChild(KAlias("home"), KPermission(LibPerm.Home), KHelp.none, LocalizedDescription("cmd.home.description"))

  def HomeAcceptCmd: ChildCommand =
    Command
      .withSender[Player, Player] { (homeOwner, _, requester) =>
        LocalizedG(homeOwner) { implicit locale =>
          for {
            optRequest           <- FtoG(homeHandler.getRequest(requester, homeOwner.uniqueId))
            request              <- optRequest.toFLift(resource.get("cmd.accept.notSentRequest"))
            requesterSuccessText <- FtoG(resource.get("cmd.accept.requesterSuccess"))
            requesterName        <- FtoG(players.name(requester))
            ownerSuccessText     <- FtoG(resource.get("cmd.accept.ownerSuccess", "requester" -> requesterName))
            res <- F.ifM(request.teleport(requester))(
              {
                val effects = (
                  requester.sendMessage(t"$Yellow$requesterSuccessText"),
                  homeOwner.sendMessage(t"$Green$ownerSuccessText"),
                  homeHandler.removeRequest(requester, homeOwner.uniqueId)
                ).tupled

                FtoG(effects).as(Command.success())
              },
              teleportError
            )
          } yield res
        }
      }
      .toChild(
        KAlias("accept"),
        KPermission(LibPerm.HomeAccept),
        description = LocalizedDescription("cmd.accept.description")
      )

  def HomeDeleteCmd: ChildCommand =
    Command
      .simple[OtherHomeArgs[NotUsed]] {
        case (sender, _, args) =>
          LocalizedG(sender) { implicit locale =>
            val effects = args.messageWithHomeName("cmd.delete.success").flatMap { successText =>
              (
                homeHandler.deleteHome(args.homeOwner.uniqueId, args.rawHomeName),
                commandSources.sendMessage(sender, t"$Green$successText")
              ).tupled
            }

            FtoG(effects).as(Command.success())
          }
      }
      .toChild(
        KAlias("delete", "remove"),
        KPermission(LibPerm.HomeDelete),
        description = LocalizedDescription("cmd.delete.description")
      )

  case class HomeGotoCmdArgs(homeOwner: OnlyOne[User], homeName: String)
  implicit val homeGotoCmdParam: Parameter[HomeGotoCmdArgs] = ParameterDeriver[HomeGotoCmdArgs].derive

  def HomeGotoCmd: ChildCommand =
    Command
      .withSender[Player, HomeGotoCmdArgs] {
        case (player, _, HomeGotoCmdArgs(OnlyOne(homeOwner), homeName)) =>
          LocalizedG(player) { implicit locale =>
            val playerUUID    = player.uniqueId
            val homeOwnerUUID = homeOwner.uniqueId
            for {
              optHome            <- homeHandler.specificHome(homeOwnerUUID, homeName)
              home               <- optHome.toFLift(homeNotFound)
              optHomeOwnerPlayer <- homeOwner.getPlayer
              isHomeOwnerOnline = optHomeOwnerPlayer.isDefined
              isInvited <- homeHandler.isInvited(player, homeOwnerUUID, home).map(_ && isHomeOwnerOnline)
              isResident = home.residents.contains(playerUUID)
              canUseGoto = isResident || isInvited
              res <- {
                if (canUseGoto) {
                  F.ifM(home.teleport(player))(
                    {
                      val effects = for {
                        ownerName <- users.name(homeOwner)
                        successTeleportText <- resource
                          .getText("cmd.goto.successTeleport", "homeName" -> homeName, "homeOwner" -> ownerName)
                        _ <- player.sendMessage(successTeleportText)
                        _ <- homeHandler.removeInvite(player, homeOwnerUUID)
                      } yield ()

                      FtoG(effects).as(Command.success())
                    },
                    teleportError
                  )
                } else {
                  optHomeOwnerPlayer.fold[G[CommandSuccess]](localizedError("cmd.goto.offlineError")) {
                    homeOwnerPlayer =>
                      val sendRequest = for {
                        ownerName  <- users.name(homeOwner)
                        playerName <- players.name(player)
                        _          <- homeHandler.addRequest(player, homeOwnerUUID, home)
                        _          <- players.sendMessage(player, t"""${Green}Sent home request to $ownerName for "$homeName"""") //TODO: Localize
                        sentRequestText <- resource.get(
                          "cmd.goto.sentRequest",
                          "player"   -> playerName,
                          "homeName" -> homeName
                        )
                        acceptButton = button(t"${Yellow}Accept", s"/home accept $playerName") //TODO: Localize
                        _ <- players.sendMessage(
                          homeOwnerPlayer,
                          t"$Yellow$sentRequestText${Text.NewLine}$Reset$acceptButton"
                        )
                      } yield Command.success()

                      FtoG(sendRequest)
                  }
                }
              }
            } yield res
          }
      }
      .toChild(
        KAlias("goto"),
        KPermission(LibPerm.HomeGoto),
        description = LocalizedDescription("cmd.goto.description") //TODO: Extended description cmd.goto.extendedDescription
      )

  def HomeInviteCmd: ChildCommand =
    Command
      .simple[OtherHomeArgs[Player]] {
        case (sender, _, args) =>
          LocalizedG(sender) { implicit locale =>
            homeHandler.addInvite(args.args, args.homeOwner.uniqueId, args.home)
            val gotoButton =
              button(
                t"$Yellow${args.messageWithHomeName("cmd.invite.goto")}",
                s"/home goto ${args.homeOwner.getName} ${args.rawHomeName}"
              )
            sender.sendMessage(
              t"$Green${args.messageWithHomeName("cmd.invite.playerSuccess", "target" -> sender.getName)}"
            )
            args.args.sendMessage(
              t"$Yellow${args
                .messageWithHomeName("cmd.invite.targetSuccess", "player" -> args.args.getName)}${Text.NEW_LINE}$RESET$gotoButton"
            )
            Command.successF()
          }
      }
      .toChild(
        KAlias("invite"),
        KPermission(LibPerm.HomeInvite),
        description = LocalizedDescription("cmd.invite.description")
      )

  def HomeLimitCmd: ChildCommand =
    Command
      .simple[OtherArgs[NotUsed]] { (sender, _, args) =>
        LocalizedG(sender) { implicit locale =>
          val limit = homeHandler.getHomeLimit(args.homeOwner)
          if (args.isOther) {
            sender.sendMessage(
              t"$YELLOW${HSHResource.get("cmd.other.limit.success", "homeOwner" -> args.homeOwner.getName, "limit" -> limit.toString)}"
            )
          } else {
            sender.sendMessage(t"$YELLOW${HSHResource.get("cmd.limit.success", "limit" -> limit.toString)}")
          }
          Command.successF()
        }
      }
      .toChild(
        KAlias("limit"),
        KPermission(LibPerm.HomeLimit),
        description = LocalizedDescription("cmd.limit.description")
      )

  def HomeListCmd: Command[CommandSource, OtherArgs[NotUsed]] =
    Command
      .simple[OtherArgs[NotUsed]] { (sender, _, args) =>
        LocalizedG(sender) { implicit locale =>
          val homes = homeHandler.allHomesForPlayer(args.homeOwner.getUniqueId).keys.toSeq
          val limit = homeHandler.getHomeLimit(args.homeOwner)

          if (homes.isEmpty) {
            if (args.isOther) {
              sender.sendMessage(
                t"$YELLOW${HSHResource.get("cmd.list.limit", "limit" -> limit.toString)}${Text.NEW_LINE}${HSHResource
                  .get("cmd.other.list.noHomes", "homwOwner"         -> args.homeOwner.getName)}"
              )
            } else {
              sender.sendMessage(
                t"$YELLOW${HSHResource.get("cmd.list.limit", "limit" -> limit.toString)}${Text.NEW_LINE}${HSHResource.get("cmd.list.noHomes")}"
              )
            }
          } else {
            val builder     = PaginationList.builder()
            val otherSuffix = if (args.isOther) s" --other ${args.homeOwner.getName}" else ""
            val homeText = homes.sorted.map { homeName =>
              val teleportButton =
                button(t"$YELLOW${HSHResource.get("cmd.list.teleport")}", s"/home $homeName$otherSuffix")
              val setButton =
                confirmButton(t"$YELLOW${HSHResource.get("cmd.list.set")}", s"/home set $homeName$otherSuffix")
              val inviteButton = confirmButton(
                t"$YELLOW${HSHResource.get("cmd.list.invite")}",
                s"/home invite <player> $homeName$otherSuffix"
              )
              val deleteButton =
                confirmButton(t"$RED${HSHResource.get("cmd.list.delete")}", s"/home delete $homeName$otherSuffix")

              val residentsButton =
                button(t"$YELLOW${HSHResource.get("cmd.list.residents")}", s"/home residents $homeName$otherSuffix")

              t""""$homeName" $teleportButton $setButton $inviteButton $residentsButton $deleteButton"""
            }

            val limitText = HSHResource.getText("cmd.list.limit", "limit" -> limit.toString)
            val newButton =
              confirmButton(t"$YELLOW${HSHResource.get("cmd.list.newHome")}", s"/home set$otherSuffix <homeName>")

            builder
              .title(t"$YELLOW${HSHResource.get("cmd.list.title")}")
              .contents(limitText +: newButton +: homeText: _*)
              .sendTo(sender)
          }

          Command.successF()
        }
      }

  def HomeSetCmd: ChildCommand =
    Command
      .withSender[Player, OtherArgs[Named[W.`"home"`.T, String]]] { (player, _, args) =>
        LocalizedG(player) { implicit locale =>
          if (!HomeCmd.command.childrenMap.keys.exists(args.args.value.startsWith)) {
            val replace         = homeHandler.homeExist(args.homeOwner.getUniqueId, args.args.value)
            val limit           = homeHandler.getHomeLimit(args.homeOwner)
            val newLimit        = if (replace) limit + 1 else limit
            val limitNotReached = homeHandler.allHomesForPlayer(args.homeOwner.getUniqueId).size < newLimit
            if (limitNotReached) {
              homeHandler.makeHome(args.homeOwner.getUniqueId, args.args.value, player.getLocation, player.getRotation)
              val homeNameText =
                if (args.isOther) s""""${args.args.value}" for ${args.homeOwner.getName}"""
                else s""""${args.args.value}""""

              player.sendMessage(t"${Green}Set $homeNameText successfully")
              Command.successF()
            } else {
              localizedError("command.error.homeLimitReached")
            }
          } else localizedError("command.error.illegalName")
        }
      }
      .toChild(KAlias("set"), KPermission(LibPerm.HomeSet), description = LocalizedDescription("cmd.set.description"))
}
