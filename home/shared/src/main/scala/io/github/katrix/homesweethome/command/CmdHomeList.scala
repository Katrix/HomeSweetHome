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

import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
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

class CmdHomeList(homeHandler: HomeHandler, parent: CmdHome)(implicit plugin: KatPlugin)
    extends LocalizedCommand(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val data = playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)

    data match {
      case Right(player) =>
        val builder = PaginationList.builder()
        builder.title(t"$YELLOW${HSHResource.get("cmd.list.title")}")
        val homes = homeHandler.allHomesForPlayer(player.getUniqueId)
        val homeText = {
          if (homes.isEmpty) Seq(t"$YELLOW${HSHResource.get("cmd.list.noHomes")}")
          else
            homes.keys.toSeq.sorted.map { homeName =>
              val teleportButton = button(t"$YELLOW${HSHResource.get("cmd.list.teleport")}", s"/home $homeName")
              val setButton      = manualButton(t"$YELLOW${HSHResource.get("cmd.list.set")}", s"/home set $homeName")
              val inviteButton =
                manualButton(t"$YELLOW${HSHResource.get("cmd.list.invite")}", s"/home invite <player> $homeName")
              val deleteButton = manualButton(t"$RED${HSHResource.get("cmd.list.delete")}", s"/home delete $homeName")

              val residentsButton =
                button(t"$YELLOW${HSHResource.get("cmd.list.residents")}", s"/home residents $homeName")

              t""""$homeName" $teleportButton $setButton $inviteButton $residentsButton $deleteButton"""
            }
        }

        val helpButton = manualButton(t"$YELLOW${HSHResource.get("cmd.list.help")}", "/home help [command]")
        val limitText  = t"${HSHResource.get("cmd.list.limit")}: ${homeHandler.getHomeLimit(player)}"
        val newButton  = manualButton(t"$YELLOW${HSHResource.get("cmd.list.newHome")}", "/home set <homeName>")

        builder.contents(limitText +: helpButton +: newButton +: homeText: _*)
        plugin.globalVersionAdapter.sendPagination(builder, src)
        CommandResult.builder().successCount(homes.size).build()
      case Left(error) => throw error
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(HSHResource.getText("cmd.list.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
      .permission(LibPerm.HomeList)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("list")
}
