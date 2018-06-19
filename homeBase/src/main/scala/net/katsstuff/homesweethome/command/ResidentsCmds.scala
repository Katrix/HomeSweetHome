package net.katsstuff.homesweethome.command

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.service.pagination.PaginationList
import org.spongepowered.api.service.user.UserStorageService
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.world.{Location, World}

import net.katsstuff.katlib.helper.Implicits._
import net.katsstuff.katlib.i18n.Localized
import net.katsstuff.homesweethome.lib.LibPerm
import net.katsstuff.homesweethome.{HSHResource, HomePlugin}
import shapeless.{Witness => W}

object ResidentsCmds {

  def residentsCmdChildren(implicit plugin: HomePlugin): Set[ChildCommand[_, _]] =
    Set(HomeResidentsAddCmd, HomeResidentsLimitCmd, HomeResidentsRemoveCmd)

  implicit def residentsParam(implicit plugin: HomePlugin): Parameter[HomeResidentsArgs] =
    new Parameter[HomeResidentsArgs] {
      private val flagParam       = valueFlagParameter[W.`"other"`.T, OnlyOne[User]]
      private val playerValidator = UserValidator[User]

      //TODO: Add source to this and test for permissions
      override def name: String = s"home -other <target>"

      override def parse(
          source: CommandSource,
          extra: Unit,
          xs: List[RawCmdArg]
      ): CommandStep[(List[RawCmdArg], HomeResidentsArgs)] = {
        for {
          t1 <- if (source.hasPermission(LibPerm.HomeOther)) flagParam.parse(source, extra, xs)
          else Right(xs -> ValueFlag(None))
          homeOwner <- t1._2.value.fold(playerValidator.validate(source))(player => Right(player.value))
          t2        <- optionParam(specificUserHomeWithNameParam(homeOwner)).parse(source, extra, t1._1)
        } yield t2._1 -> HomeResidentsArgs(t1._2.value.isDefined, t2._2, homeOwner)
      }

      override def suggestions(
          source: CommandSource,
          extra: Location[World],
          xs: List[RawCmdArg]
      ): Either[List[RawCmdArg], Seq[String]] = {
        for {
          ys <- (if (source.hasPermission(LibPerm.HomeOther)) flagParam.suggestions(source, extra, xs) else Left(xs)).left
          zs <- homeParam.suggestions(source, extra, ys)
        } yield zs
      }

      override def usage(source: CommandSource): String = {
        val base = "[home]"
        if (source.hasPermission(LibPerm.HomeOther)) s"$base --other <player>" else base
      }
    }

  case class HomeResidentsArgs(isOther: Boolean, home: Option[HomeWithName], homeOwner: User)

  def HomeResidentsCmd(implicit plugin: HomePlugin): ChildCommand[CommandSource, HomeResidentsArgs] =
    Command
      .withChildren[HomeResidentsArgs](residentsCmdChildren) {
        case (sender, _, args) =>
          Localized(sender) { implicit locale =>
            val otherSuffix = if (args.isOther) s" --other ${args.homeOwner.getName}" else ""
            args.home match {
              case Some(HomeWithName(homeName, home)) =>
                val residents = home.residents
                val limit     = homeHandler.getResidentLimit(args.homeOwner)

                val builder     = PaginationList.builder()
                val title       = t"$YELLOW${HSHResource.get("cmd.residents.homeTitle", "homeName" -> homeName)}"
                val userStorage = Sponge.getServiceManager.provideUnchecked(classOf[UserStorageService])

                val residentText = {
                  if (residents.isEmpty) Seq(t"$YELLOW${HSHResource.get("cmd.residents.noResidents")}")
                  else
                    residents
                      .flatMap(uuid => userStorage.get(uuid).toOption.map(_.getName).getOrElse(uuid.toString))
                      .sorted
                      .map { residentName =>
                        val deleteButton =
                          confirmButton(
                            t"$RED${HSHResource.get("cmd.residents.delete")}",
                            s"/home residents remove $homeName $residentName$otherSuffix"
                          )

                        t"$residentName $deleteButton"
                      }
                }

                val limitText = HSHResource.getText("cmd.residents.limit", "limit" -> limit.toString)
                val newButton =
                  confirmButton(
                    t"$YELLOW${HSHResource.get("cmd.residents.newResident")}",
                    s"/home residents add$otherSuffix $homeName <player>"
                  )

                builder.title(title).contents(limitText +: newButton +: residentText: _*).sendTo(sender)

                Command.successStep()
              case None =>
                val residents = homeHandler.allHomesForPlayer(args.homeOwner.getUniqueId).mapValues(_.residents)
                val limit     = homeHandler.getResidentLimit(args.homeOwner)
                val builder   = PaginationList.builder()
                val title =
                  t"""$YELLOW${HSHResource.get("cmd.residents.playerTitle", "player" -> args.homeOwner.getName)}"""
                val userStorage = Sponge.getServiceManager.provideUnchecked(classOf[UserStorageService])

                val residentText = {
                  if (residents.isEmpty) Seq(t"$YELLOW${HSHResource.get("cmd.residents.noHomes")}")
                  else
                    residents.toSeq
                      .sortBy(_._1)
                      .map {
                        case (homeName, homeResidentsUuids) =>
                          val details = button(
                            t"$YELLOW${HSHResource.get("cmd.residents.details")}",
                            s"/home residents $homeName$otherSuffix"
                          )
                          if (homeResidentsUuids.isEmpty)
                            t"$homeName: $YELLOW${HSHResource.get("cmd.residents.noResidents")}$RESET $details"
                          else {
                            val homeResidents = homeResidentsUuids
                              .flatMap(uuid => userStorage.get(uuid).toOption.map(_.getName).getOrElse(uuid.toString))
                            t""""$homeName": $YELLOW${homeResidents.mkString(", ")}$RESET $details"""
                          }
                      }
                }

                val limitText = t"Limit: $limit"

                builder.title(title).contents(limitText +: residentText: _*).sendTo(sender)

                Command.successStep()
            }
          }
      }
      .toChild(
        Alias("residents", "res"),
        Permission(LibPerm.HomeResident),
        shortDescription = LocalizedDescription("cmd.residents.description")
      )

  def HomeResidentsAddCmd(
      implicit plugin: HomePlugin
  ): ChildCommand[CommandSource, OtherHomeArgs[Named[W.`"resident"`.T, OnlyOne[User]]]] =
    Command
      .simple[OtherHomeArgs[Named[W.`"resident"`.T, OnlyOne[User]]]] {
        case (sender, _, args) =>
          Localized(sender) { implicit locale =>
            val Named(OnlyOne(playerArgs)) = args.args
            val limitNotReached            = args.home.residents.size < homeHandler.getResidentLimit(args.homeOwner)
            if (limitNotReached) {
              if (!args.home.residents.contains(playerArgs.getUniqueId)) {
                val newHome = args.home.addResident(playerArgs.getUniqueId)
                homeHandler.updateHome(args.homeOwner.getUniqueId, args.rawHomeName, newHome)
                sender.sendMessage(
                  t"$GREEN${args.messageWithHomeName("cmd.residentsAdd.playerSuccess", "target" -> args.homeName)}"
                )
                playerArgs.getPlayer.toOption.foreach(
                  _.sendMessage(
                    t"$YELLOW${HSHResource.get("cmd.residentsAdd.targetSuccess", "homeName" -> args.rawHomeName, "player" -> args.homeOwner.getName)}"
                  )
                )
                Command.successStep()
              } else {
                Command.errorStep(
                  args.messageWithHomeName("cmd.residentsAdd.alreadyResident", "target" -> playerArgs.getName).toPlain
                )
              }
            } else {
              Command.errorStep(HSHResource.get("command.error.residentLimitReached"))
            }
          }
      }
      .toChild(
        Alias("add"),
        Permission(LibPerm.HomeResidentAdd),
        shortDescription = LocalizedDescription("cmd.residentsAdd.description")
      )

  def HomeResidentsLimitCmd(implicit plugin: HomePlugin): ChildCommand[CommandSource, OtherArgs[NotUsed]] =
    Command
      .simple[OtherArgs[NotUsed]] { (sender, _, args) =>
        Localized(sender) { implicit locale =>
          val limit = homeHandler.getResidentLimit(args.homeOwner)
          if (args.isOther) {
            sender.sendMessage(t"$YELLOW${HSHResource
              .get("cmd.other.residentsLimit.success", "homeOwner" -> args.homeOwner.getName, "limit" -> limit.toString)}")
          } else {
            sender.sendMessage(t"$YELLOW${HSHResource.get("cmd.residentsLimit.success", "limit" -> limit.toString)}")
          }
          Command.successStep()
        }
      }
      .toChild(
        Alias("limit"),
        Permission(LibPerm.HomeResidentLimit),
        shortDescription = LocalizedDescription("cmd.residentsLimit.description")
      )

  def HomeResidentsRemoveCmd(
      implicit plugin: HomePlugin
  ): ChildCommand[CommandSource, OtherHomeArgs[Named[W.`"resident"`.T, OnlyOne[User]]]] =
    Command
      .simple[OtherHomeArgs[Named[W.`"resident"`.T, OnlyOne[User]]]] {
        case (sender, _, args) =>
          Localized(sender) { implicit locale =>
            val Named(OnlyOne(playerArgs)) = args.args
            if (args.home.residents.contains(playerArgs.getUniqueId)) {
              val newHome = args.home.removeResident(playerArgs.getUniqueId)
              homeHandler.updateHome(args.homeOwner.getUniqueId, args.rawHomeName, newHome)

              sender.sendMessage(
                t"$GREEN${args.messageWithHomeName("cmd.residentsRemove.playerSuccess", "target" -> playerArgs.getName)}"
              )
              playerArgs.getPlayer.toOption.foreach(
                _.sendMessage(
                  t"$YELLOW${HSHResource.get("cmd.residentsRemove.targetSuccess", "homeName" -> args.rawHomeName, "player" -> args.homeOwner.getName)}"
                )
              )
              Command.successStep()
            } else {
              Command.errorStep(
                args.messageWithHomeName("cmd.residentsRemove.notAResident", "target" -> playerArgs.getName).toPlain
              )
            }
          }
      }
      .toChild(
        Alias("remove", "delete"),
        Permission(LibPerm.HomeResidentRemove),
        shortDescription = LocalizedDescription("cmd.residentsRemove.description")
      )
}
