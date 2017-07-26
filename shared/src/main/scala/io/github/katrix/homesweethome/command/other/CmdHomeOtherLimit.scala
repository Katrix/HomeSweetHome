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
import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.LibPerm
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.LocalizedCommand
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import io.github.katrix.katlib.lib.LibCommonCommandKey

class CmdHomeOtherLimit(homeHandler: HomeHandler, parent: CmdHomeOther)(implicit plugin: KatPlugin) extends LocalizedCommand(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    args.getOne[User](LibCommonCommandKey.Player).toOption match {
      case Some(homeOwner) =>
        val limit = homeHandler.getHomeLimit(homeOwner)
        src.sendMessage(t"$YELLOW${HSHResource.get("cmd.other.limit.success", "homeOwner" -> homeOwner.getName, "limit" -> limit.toString)}")
        CommandResult.builder().successCount(limit).build()
      case None => throw playerNotFoundErrorLocalized
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] = Some(HSHResource.getText("cmd.other.limit.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.user(LibCommonCommandKey.Player))
      .description(this)
      .permission(LibPerm.HomeOtherLimit)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("limit")
}
