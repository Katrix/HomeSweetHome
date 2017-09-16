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

import java.util.Locale

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.HSHResource
import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import io.github.katrix.katlib.lib.LibCommonTCommandKey

class CmdHomeGoto(homeHandler: HomeHandler, parent: CmdHome)(implicit plugin: KatPlugin)
    extends LocalizedCommand(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = for {
      player    <- playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)
      homeOwner <- args.one(LibCommonTCommandKey.Player).toRight(playerNotFoundErrorLocalized)
      homeName  <- args.one(LibCommandKey.HomeName).toRight(invalidParameterErrorLocalized)
      home      <- homeHandler.specificHome(homeOwner.getUniqueId, homeName).toRight(homeNotFoundError)
    } yield {
      val isResident = home.residents.contains(player.getUniqueId)
      val isInvited  = homeHandler.isInvited(player, homeOwner.getUniqueId, home) && homeOwner.isOnline
      (player, homeOwner, homeName, home, isResident || isInvited)
    }

    data match {
      case Right((player, homeOwner, homeName, home, true)) if home.teleport(player) =>
        src.sendMessage(
          t"$GREEN${HSHResource.get("cmd.goto.successTeleport", "homeName" -> homeName, "homeOwner" -> homeOwner.getName)}"
        )
        homeHandler.removeInvite(player, homeOwner.getUniqueId)
        CommandResult.success()
      case Right((_, _, _, _, true)) => throw teleportError
      case Right((player, homeOwner, homeName, home, false)) if homeOwner.isOnline =>
        homeHandler.addRequest(player, homeOwner.getUniqueId, home)
        src.sendMessage(
          t"$GREEN${HSHResource.get("cmd.goto.successRequest", "homeOwner" -> homeOwner.getName, "homeName" -> homeName)}"
        )
        val acceptButton = button(t"${YELLOW}Accept", s"/home accept ${player.getName}")
        homeOwner.getPlayer
          .get()
          .sendMessage(
            t"$YELLOW${HSHResource
              .get("cmd.goto.sentRequest", "player" -> player.getName, "homeName" -> homeName)}${Text.NEW_LINE}$RESET$acceptButton"
          )
        CommandResult.success()
      case Right((_, _, _, _, false)) => throw new CommandException(HSHResource.getText("cmd.goto.offlineError"))
      case Left(error)                => throw error
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(HSHResource.getText("cmd.goto.description"))
  override def localizedExtendedDescription(implicit locale: Locale): Option[Text] =
    Some(HSHResource.getText("cmd.goto.extendedDescription"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(
        GenericArguments.user(LibCommonTCommandKey.Player),
        GenericArguments.remainingJoinedStrings(LibCommandKey.Home)
      )
      .description(this)
      .extendedDescription(this)
      .permission(LibPerm.HomeGoto)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("goto")
}
