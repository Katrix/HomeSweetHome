package net.katsstuff.homesweethome.command

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.{Player, User}
import org.spongepowered.api.service.pagination.PaginationList
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.homesweethome.lib.LibPerm
import net.katsstuff.homesweethome.{HSHResource, HomePlugin}
import net.katsstuff.scammander.sponge.CommandInfo
import shapeless.{Witness => W}

object HomeCmds {
  def homeCmdChildren(implicit plugin: HomePlugin): Set[ChildCommand[_, _]] = Set(
    HomeSetCmd,
    HomeDeleteCmd,
    ChildCommand(Set("list"), HomeListCmd),
    HomeLimitCmd,
    HomeAcceptCmd,
    HomeGotoCmd,
    HomeInviteCmd,
    ResidentsCmds.HomeResidentsCmd,
    HomeHelpCmd
  )

  def HomeHelpCmd(implicit plugin: HomePlugin): ChildCommand[CommandSource, ZeroOrMore[String]] =
    helpCommand(
      t"HomeSweetHome commands",
      Set(
        ChildCommand(Set("home"), HomeCmd),
        ChildCommand(Set("homesweethome"), BaseCmds.HomeSweetHomeCmd),
        ChildCommand(Set("homes"), HomeListCmd)
      )
    ).toChild(Alias("help"), shortDescription = LocalizedDescription("cmd.help.description"))

  //TODO: Support no args here use Residents args code
  def HomeCmd(implicit plugin: HomePlugin): SpongeCommandWrapper[Player, OtherHomeArgs[NotUsed]] =
    Command
      .withSenderAndChildren[Player, OtherHomeArgs[NotUsed]](homeCmdChildren) {
        case (player, _, args) =>
          Localized(player) { implicit locale =>
            if (args.home.teleport(player)) {
              player.sendMessage(t"$GREEN${args.messageWithHomeName("cmd.home.success")}")
              Command.successStep()
            } else {
              Left(teleportError)
            }
          }
      }
      .toSponge(CommandInfo(Permission(LibPerm.Home), shortDescription = LocalizedDescription("cmd.home.description")))

  def HomeAcceptCmd(implicit plugin: HomePlugin): ChildCommand[Player, Player] =
    Command
      .withSender[Player, Player] { (homeOwner, _, requester) =>
        Localized(homeOwner) { implicit locale =>
          homeHandler
            .getRequest(requester, homeOwner.getUniqueId)
            .toStep(HSHResource.get("cmd.accept.notSentRequest"))
            .flatMap { home =>
              if (home.teleport(requester)) {
                requester.sendMessage(t"$YELLOW${HSHResource.get("cmd.accept.requesterSuccess")}")
                homeOwner.sendMessage(
                  t"$GREEN${HSHResource.get("cmd.accept.ownerSuccess", "requester" -> requester.getName)}"
                )
                homeHandler.removeRequest(requester, homeOwner.getUniqueId)
                Command.successStep()
              } else Left(teleportError)
            }
        }
      }
      .toChild(
        Alias("accept"),
        Permission(LibPerm.HomeAccept),
        shortDescription = LocalizedDescription("cmd.accept.description")
      )

  def HomeDeleteCmd(implicit plugin: HomePlugin): ChildCommand[CommandSource, OtherHomeArgs[NotUsed]] =
    Command
      .simple[OtherHomeArgs[NotUsed]] {
        case (sender, _, args) =>
          Localized(sender) { implicit locale =>
            homeHandler.deleteHome(args.homeOwner.getUniqueId, args.rawHomeName)
            sender.sendMessage(t"$GREEN${args.messageWithHomeName("cmd.delete.success")}")
            Command.successStep()
          }
      }
      .toChild(
        Alias("delete", "remove"),
        Permission(LibPerm.HomeDelete),
        shortDescription = LocalizedDescription("cmd.delete.description")
      )

  case class HomeGotoCmdArgs(homeOwner: OnlyOne[User], homeName: String)
  implicit val homeGotoCmdParam: Parameter[HomeGotoCmdArgs] = ParameterDeriver[HomeGotoCmdArgs].derive

  def HomeGotoCmd(implicit plugin: HomePlugin): ChildCommand[Player, HomeGotoCmdArgs] =
    Command
      .withSender[Player, HomeGotoCmdArgs] {
        case (player, _, HomeGotoCmdArgs(OnlyOne(homeOwner), homeName)) =>
          Localized(player) { implicit locale =>
            homeHandler.specificHome(homeOwner.getUniqueId, homeName).toStep(HomeNotFound).flatMap { home =>
              val isResident = home.residents.contains(player.getUniqueId)
              val isInvited  = homeHandler.isInvited(player, homeOwner.getUniqueId, home) && homeOwner.isOnline
              val canUseGoto = isResident || isInvited

              if (canUseGoto) {
                if (home.teleport(player)) {
                  player.sendMessage(
                    t"$GREEN${HSHResource.get("cmd.goto.successTeleport", "homeName" -> homeName, "homeOwner" -> homeOwner.getName)}"
                  )
                  homeHandler.removeInvite(player, homeOwner.getUniqueId)
                  Command.successStep()
                } else Left(teleportError)
              } else {
                if (homeOwner.isOnline) {
                  homeHandler.addRequest(player, homeOwner.getUniqueId, home)
                  player.sendMessage(t"""${GREEN}Sent home request to ${homeOwner.getName} for "$homeName"""")
                  val acceptButton = button(t"${YELLOW}Accept", s"/home accept ${player.getName}") //TODO: Localize
                  homeOwner.getPlayer
                    .get()
                    .sendMessage(t"$YELLOW${HSHResource
                      .get("cmd.goto.sentRequest", "player" -> player.getName, "homeName" -> homeName)}${Text.NEW_LINE}$RESET$acceptButton")
                  Command.successStep()
                } else {
                  Command.errorStep(HSHResource.get("cmd.goto.offlineError"))
                }
              }
            }
          }
      }
      .toChild(
        Alias("goto"),
        Permission(LibPerm.HomeGoto),
        shortDescription = LocalizedDescription("cmd.goto.description") //TODO: Extended description cmd.goto.extendedDescription
      )

  def HomeInviteCmd(implicit plugin: HomePlugin): ChildCommand[CommandSource, OtherHomeArgs[Player]] =
    Command
      .simple[OtherHomeArgs[Player]] {
        case (sender, _, args) =>
          Localized(sender) { implicit locale =>
            homeHandler.addInvite(args.args, args.homeOwner.getUniqueId, args.home)
            val gotoButton =
              button(
                t"$YELLOW${args.messageWithHomeName("cmd.invite.goto")}",
                s"/home goto ${args.homeOwner.getName} ${args.rawHomeName}"
              )
            sender.sendMessage(
              t"$GREEN${args.messageWithHomeName("cmd.invite.playerSuccess", "target" -> sender.getName)}"
            )
            args.args.sendMessage(
              t"$YELLOW${args
                .messageWithHomeName("cmd.invite.targetSuccess", "player" -> args.args.getName)}${Text.NEW_LINE}$RESET$gotoButton"
            )
            Command.successStep()
          }
      }
      .toChild(
        Alias("invite"),
        Permission(LibPerm.HomeInvite),
        shortDescription = LocalizedDescription("cmd.invite.description")
      )

  def HomeLimitCmd(implicit plugin: HomePlugin): ChildCommand[CommandSource, OtherArgs[NotUsed]] =
    Command
      .simple[OtherArgs[NotUsed]] { (sender, _, args) =>
        Localized(sender) { implicit locale =>
          val limit = homeHandler.getHomeLimit(args.homeOwner)
          if (args.isOther) {
            sender.sendMessage(
              t"$YELLOW${HSHResource.get("cmd.other.limit.success", "homeOwner" -> args.homeOwner.getName, "limit" -> limit.toString)}"
            )
          } else {
            sender.sendMessage(t"$YELLOW${HSHResource.get("cmd.limit.success", "limit" -> limit.toString)}")
          }
          Command.successStep()
        }
      }
      .toChild(
        Alias("limit"),
        Permission(LibPerm.HomeLimit),
        shortDescription = LocalizedDescription("cmd.limit.description")
      )

  def HomeListCmd(implicit plugin: HomePlugin): SpongeCommandWrapper[CommandSource, OtherArgs[NotUsed]] =
    Command
      .simple[OtherArgs[NotUsed]] { (sender, _, args) =>
        Localized(sender) { implicit locale =>
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

          Command.successStep()
        }
      }
      .toSponge(
        CommandInfo(Permission(LibPerm.HomeList), shortDescription = LocalizedDescription("cmd.list.description"))
      )

  def HomeSetCmd(implicit plugin: HomePlugin): ChildCommand[Player, OtherArgs[Named[W.`"home"`.T, String]]] =
    Command
      .withSender[Player, OtherArgs[Named[W.`"home"`.T, String]]] { (player, _, args) =>
        Localized(player) { implicit locale =>
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

              player.sendMessage(t"${GREEN}Set $homeNameText successfully")
              Command.successStep()
            } else {
              Command.errorStep(HSHResource.get("command.error.homeLimitReached"))
            }
          } else Command.errorStep(HSHResource.get("command.error.illegalName"))
        }
      }
      .toChild(
        Alias("set"),
        Permission(LibPerm.HomeSet),
        shortDescription = LocalizedDescription("cmd.set.description")
      )
}
