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
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.HSHResource
import io.github.katrix.homesweethome.command.other.CmdHomeOther
import io.github.katrix.homesweethome.command.residents.CmdHomeResidents
import io.github.katrix.homesweethome.home.{Home, HomeHandler}
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.{CommandBase, LocalizedCommand}
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized

class CmdHome(homeHandler: HomeHandler)(implicit plugin: KatPlugin) extends LocalizedCommand(None) {

  val homeList = new CmdHomeList(homeHandler, this)

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    if (args.hasAny(LibCommandKey.Home)) {
      val data = for {
        player           <- playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)
        (home, homeName) <- args.one(LibCommandKey.Home).toRight(homeNotFoundError)
      } yield (player, homeName, home)

      data match {
        case Right((player, homeName, home)) if home.teleport(player) =>
          src.sendMessage(t"$GREEN${HSHResource.get("cmd.home.success", "homeName" -> homeName)}")
          CommandResult.success()
        case Right(_)    => throw teleportError
        case Left(error) => throw error
      }
    } else homeList.execute(src, args)
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(HSHResource.getText("cmd.home.description"))
  override def localizedExtendedDescription(implicit locale: Locale): Option[Text] =
    Some(t"${HSHResource.getText(
      "cmd.home.extendedDescription",
      "command" ->
        Text
          .builder("/home set <name of home>")
          .onShiftClick(TextActions.insertText("/home set <name of home>"))
    )}")

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
      .extendedDescription(this)
      .permission(LibPerm.HomeTp)
      .arguments(GenericArguments.optional(new CommandElementHome(LibCommandKey.Home, homeHandler)))
      .executor(this)
      .children(this)
      .build()

  override def children: Seq[CommandBase] = Seq(
    homeList,
    new CmdHomeSet(homeHandler, this),
    new CmdHomeDelete(homeHandler, this),
    new CmdHomeAccept(homeHandler, this),
    new CmdHomeGoto(homeHandler, this),
    new CmdHomeInvite(homeHandler, this),
    new CmdHomeLimit(homeHandler, this),
    new CmdHomeOther(homeHandler, this),
    new CmdHomeResidents(homeHandler, this),
    plugin.pluginCmd.cmdHelp
  )

  override def aliases: Seq[String] = Seq("home")
}
