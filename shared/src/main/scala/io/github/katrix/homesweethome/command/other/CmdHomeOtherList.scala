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
import org.spongepowered.api.service.pagination.PaginationList
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

class CmdHomeOtherList(homeHandler: HomeHandler, parent: CmdHomeOther)(implicit plugin: KatPlugin) extends LocalizedCommand(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = for {
      target <- args.getOne[User](LibCommonCommandKey.Player).toOption.toRight(playerNotFoundErrorLocalized)
    } yield (target, homeHandler.allHomesForPlayer(target.getUniqueId).keys.toSeq, homeHandler.getHomeLimit(target))

    data match {
      case Right((homeOwner, Seq(), _)) =>
        src.sendMessage(t"$YELLOW${HSHResource.get("cmd.other.list.noHomes", "homeOwner" -> homeOwner.getName)}")
        CommandResult.empty()
      case Right((homeOwnerUser, homes, limit)) =>
        val builder   = PaginationList.builder()
        val homeOwner = homeOwnerUser.getName
        builder.title(t"$YELLOW${HSHResource.get("cmd.other.list.title", "homeOwner" -> homeOwner)}")

        val homeText = homes.sorted.map { homeName =>
          val teleportButton = button(t"$YELLOW${HSHResource.get("cmd.list.teleport")}", s"/home other $homeOwner $homeName")
          val setButton      = manualButton(t"$YELLOW${HSHResource.get("cmd.list.set")}", s"/home other set $homeOwner $homeName")
          val inviteButton   = manualButton(t"$YELLOW${HSHResource.get("cmd.list.invite")}", s"/home other invite $homeOwner <player> $homeName")
          val deleteButton   = manualButton(t"$RED${HSHResource.get("cmd.list.delete")}", s"/home other delete $homeOwner $homeName")

          val residentsButton = button(t"$YELLOW${HSHResource.get("cmd.list.residents")}", s"/home other residents $homeOwner $homeName")

          t""""$homeName" $teleportButton $setButton $inviteButton $residentsButton $deleteButton"""
        }

        val limitText = t"${HSHResource.get("cmd.list.limit")}: $limit"
        val newButton = manualButton(t"$YELLOW${HSHResource.get("cmd.list.newHome")}", s"/home other set $homeOwner <homeName>")

        builder.contents(limitText +: newButton +: homeText: _*)
        builder.sendTo(src)
        CommandResult.builder().successCount(homes.size).build()
      case Left(error) => throw error
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] = Some(HSHResource.getText("cmd.other.list.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
      .permission(LibPerm.HomeOtherList)
      .arguments(GenericArguments.player(LibCommonCommandKey.Player))
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("list", "homes")
}
