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
package other

import java.util.Locale

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.{Player, User}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.HSHResource
import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import io.github.katrix.katlib.lib.LibCommonCommandKey

class CmdHomeOtherInvite(homeHandler: HomeHandler, parent: CmdHomeOther)(implicit plugin: KatPlugin)
    extends LocalizedCommand(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = for {
      player    <- playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)
      homeOwner <- args.getOne[User]("homeOwner".text).toOption.toRight(playerNotFoundErrorLocalized)
      target    <- args.getOne[Player](LibCommonCommandKey.Player).toOption.toRight(playerNotFoundErrorLocalized)
      homeName  <- args.getOne[String](LibCommandKey.Home).toOption.toRight(invalidParameterErrorLocalized)
      home      <- homeHandler.specificHome(homeOwner.getUniqueId, homeName).toRight(homeNotFoundError)
    } yield (player, homeOwner, target, homeName, home)

    data match {
      case Right((player, homeOwner, target, homeName, home)) =>
        homeHandler.addInvite(target, homeOwner.getUniqueId, home)
        val gotoButton =
          button(
            t"$YELLOW${HSHResource.get("cmd.invite.goto", "homeName" -> homeName)}",
            s"/home goto ${homeOwner.getName} $homeName"
          )
        src.sendMessage(
          t"$GREEN${HSHResource.get("cmd.other.invite.playerSuccess", "target" -> target.getName, "homeName" -> homeName, "homeOwner" -> homeOwner.getName)}"
        )
        target.sendMessage(
          t"$YELLOW${HSHResource
            .get("cmd.other.invite.targetSuccess", "homeName" -> homeName, "homeOwner" -> homeOwner.getName, "player" -> player.getName)}${Text.NEW_LINE}$RESET$gotoButton"
        )
        CommandResult.success()
      case Left(error) => throw error
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(HSHResource.getText("cmd.other.invite.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(
        GenericArguments.user("homeOwner".text),
        GenericArguments.player(LibCommonCommandKey.Player),
        GenericArguments.remainingJoinedStrings(LibCommandKey.Home)
      )
      .description(this)
      .permission(LibPerm.HomeOtherInvite)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("invite")
}
