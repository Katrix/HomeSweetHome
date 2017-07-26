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
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.HSHResource
import io.github.katrix.homesweethome.command.other.residents.CmdHomeOtherResidents
import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.{CommandBase, LocalizedCommand}
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import io.github.katrix.katlib.lib.LibCommonCommandKey

class CmdHomeOther(homeHandler: HomeHandler, parent: CmdHome)(implicit plugin: KatPlugin) extends LocalizedCommand(Some(parent)) {

  private val list = new CmdHomeOtherList(homeHandler, this)

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    if (args.hasAny(LibCommandKey.Home)) {
      val data = for {
        player   <- playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)
        target   <- args.getOne[User](LibCommonCommandKey.Player).toOption.toRight(playerNotFoundErrorLocalized)
        homeName <- args.getOne[String](LibCommandKey.Home).toOption.toRight(invalidParameterErrorLocalized)
        home     <- homeHandler.specificHome(target.getUniqueId, homeName).toRight(homeNotFoundError)
      } yield (player, target, homeName, home)

      data match {
        case Right((player, target, homeName, home)) if home.teleport(player) =>
          src.sendMessage(t"$GREEN${HSHResource.get("cmd.other.home.success", "homeName" -> homeName, "target" -> target.getName)}")
          CommandResult.success()
        case Right(_)    => throw teleportError
        case Left(error) => throw error
      }
    } else list.execute(src, args)
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] = Some(HSHResource.getText("cmd.other.home.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
      .permission(LibPerm.HomeOtherTp)
      .arguments(
        GenericArguments.user(LibCommonCommandKey.Player),
        GenericArguments.optional(GenericArguments.remainingJoinedStrings(LibCommandKey.Home))
      )
      .executor(this)
      .children(this)
      .build()

  override def children: Seq[CommandBase] = Seq(
    list,
    new CmdHomeOtherSet(homeHandler, this),
    new CmdHomeOtherDelete(homeHandler, this),
    new CmdHomeOtherLimit(homeHandler, this),
    new CmdHomeOtherInvite(homeHandler, this),
    new CmdHomeOtherResidents(homeHandler, this)
  )

  override def aliases: Seq[String] = Seq("other")
}
