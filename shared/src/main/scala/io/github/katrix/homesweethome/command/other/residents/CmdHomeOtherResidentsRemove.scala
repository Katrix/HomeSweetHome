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
package other.residents

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

class CmdHomeOtherResidentsRemove(homeHandler: HomeHandler, parent: CmdHomeOtherResidents)(implicit plugin: KatPlugin)
    extends LocalizedCommand(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = for {
      homeOwner <- args.one(LibCommandKey.HomeOwner).toRight(playerNotFoundErrorLocalized)
      target    <- args.one(LibCommonTCommandKey.Player).toRight(playerNotFoundErrorLocalized)
      homeName  <- args.one(LibCommandKey.HomeName).toRight(invalidParameterErrorLocalized)
      home      <- homeHandler.specificHome(homeOwner.getUniqueId, homeName).toRight(homeNotFoundError)
    } yield (homeOwner, target, home, homeName)

    data match {
      case Right((homeOwner, target, home, homeName)) if home.residents.contains(target.getUniqueId) =>
        val newHome = home.removeResident(target.getUniqueId)
        homeHandler.updateHome(homeOwner.getUniqueId, homeName, newHome)
        src.sendMessage(
          t"$GREEN${HSHResource.get("cmd.other.residentsRemove.playerSuccess", "target" -> target.getName, "homeName" -> homeName, "homeOwner" -> homeOwner.getName)}"
        )
        target.sendMessage(
          t"$YELLOW${HSHResource.get("cmd.other.residentsRemove.targetSuccess", "homeName" -> homeName, "homeOwner" -> homeOwner.getName)}"
        )
        CommandResult.success()
      case Right((homeOwner, target, _, homeName)) =>
        throw new CommandException(
          HSHResource.getText(
            "cmd.other.residentsRemove.notAResident",
            "target"    -> target.getName,
            "homeName"  -> homeName,
            "homeOwner" -> homeOwner.getName
          )
        )
      case Left(error) => throw error
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(HSHResource.getText("cmd.other.residentsRemove.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(
        GenericArguments.user(LibCommandKey.HomeOwner),
        GenericArguments.player(LibCommonTCommandKey.Player),
        GenericArguments.remainingJoinedStrings(LibCommandKey.HomeName)
      )
      .description(this)
      .permission(LibPerm.HomeOtherResidentsAdd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("remove", "delete")
}
