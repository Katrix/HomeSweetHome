/*
 * This file is part of HomeSweetHome, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.katrix.homesweethome.command
package residents

import java.util.Locale

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.service.pagination.PaginationList
import org.spongepowered.api.service.user.UserStorageService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.HSHResource
import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.{CommandBase, LocalizedCommand}
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized

class CmdHomeResidents(homeHandler: HomeHandler, parent: CmdHome)(implicit plugin: KatPlugin)
    extends LocalizedCommand(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    if (args.hasAny(LibCommandKey.Home)) {
      val data = for {
        player <- playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)
        home   <- args.one(LibCommandKey.Home).toRight(homeNotFoundError)
      } yield (home._2, home._1.residents, homeHandler.getResidentLimit(player))

      data match {
        case Right((homeName, residents, limit)) =>
          val userStorage = Sponge.getServiceManager.provideUnchecked(classOf[UserStorageService])
          val builder     = PaginationList.builder()
          builder.title(t"$YELLOW${HSHResource.get("cmd.residents.homeTitle", "homeName" -> homeName)}")

          val residentText = {
            if (residents.isEmpty) Seq(t"$YELLOW${HSHResource.get("cmd.residents.noResidents")}")
            else
              residents
                .map(uuid => userStorage.get(uuid).toOption.map(_.getName))
                .collect { case Some(str) => str }
                .sorted
                .map { residentName =>
                  val deleteButton = manualButton(
                    t"$RED${HSHResource.get("cmd.residents.delete")}",
                    s"/home residents remove $residentName $homeName"
                  )

                  t"$residentName $deleteButton"
                }
          }

          val limitText = t"${HSHResource.get("cmd.residents.limit")}: $limit"
          val newButton = manualButton(
            t"$YELLOW${HSHResource.get("cmd.residents.newResident")}",
            s"/home residents add <player> $homeName"
          )

          builder.contents(limitText +: newButton +: residentText: _*)

          plugin.globalVersionAdapter.sendPagination(builder, src)
          CommandResult.builder().successCount(residents.size).build()
        case Left(error) => throw error
      }
    } else {
      val data = for {
        player <- playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)
      } yield
        (
          player,
          homeHandler.allHomesForPlayer(player.getUniqueId).mapValues(_.residents),
          homeHandler.getResidentLimit(player)
        )

      data match {
        case Right((player, residents, limit)) =>
          val userStorage = Sponge.getServiceManager.provideUnchecked(classOf[UserStorageService])
          val builder     = PaginationList.builder()
          builder.title(t"$YELLOW${HSHResource.get("cmd.residents.playerTitle", "player" -> player.getName)}")

          val residentText = {
            if (residents.isEmpty) Seq(t"$YELLOW${HSHResource.get("cmd.residents.noHomes")}")
            else
              residents.toSeq
                .sortBy(_._1)
                .map {
                  case (homeName, residentsUuids) =>
                    val details =
                      button(t"$YELLOW${HSHResource.get("cmd.residents.details")}", s"/home residents $homeName")

                    val residentsText =
                      if (residentsUuids.isEmpty) HSHResource.get("cmd.residents.noResidents")
                      else residentsUuids.flatMap(userStorage.get(_).toOption.map(_.getName)).mkString(", ")

                    t"$homeName: $YELLOW$residentsText$RESET $details"
                }
          }

          val limitText = t"${HSHResource.get("cmd.residents.limit")}: $limit"

          builder.contents(limitText +: residentText: _*)

          plugin.globalVersionAdapter.sendPagination(builder, src)
          CommandResult.builder().successCount(residents.values.flatten.size).build()

        case Left(e) => throw e
      }
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(HSHResource.getText("cmd.residents.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.optional(new CommandElementHome(LibCommandKey.Home, homeHandler)))
      .description(this)
      .permission(LibPerm.HomeResidentsList)
      .executor(this)
      .children(this)
      .build()

  override def aliases: Seq[String] = Seq("residents", "res")

  override def children: Seq[CommandBase] =
    Seq(
      new CmdHomeResidentsAdd(homeHandler, this),
      new CmdHomeResidentsLimit(homeHandler, this),
      new CmdHomeResidentsRemove(homeHandler, this)
    )
}
