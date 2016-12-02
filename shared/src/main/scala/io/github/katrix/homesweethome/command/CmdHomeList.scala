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

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.LibPerm
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._

class CmdHomeList(homeHandler: HomeHandler, parent: CmdHome)(implicit plugin: KatPlugin) extends CommandBase(Some(parent)) {

	override def execute(src: CommandSource, args: CommandContext): CommandResult = {
		val data = for {
			player <- playerTypeable.cast(src).toRight(nonPlayerError).right
		} yield (homeHandler.allHomesForPlayer(player.getUniqueId).keys.toSeq, homeHandler.getHomeLimit(player))

		data match {
			case Right((Seq(), _)) =>
				src.sendMessage(t"${YELLOW}You don't have any homes")
				CommandResult.empty()
			case Right((homes, limit)) =>
				val builder = Sponge.getServiceManager.provideUnchecked[PaginationService].builder()
				builder.title(t"${YELLOW}Homes")
				val homeText = homes.sorted.map { homeName =>
					val setButton = shiftButton(t"${YELLOW}Set", s"/home set $homeName")
					val inviteButton = shiftButton(t"${YELLOW}Invite", s"/home invite <player> $homeName")
					val deleteButton = shiftButton(t"${RED}Delete", s"/home delete $homeName")

					val residentsButton = shiftButton(t"${YELLOW}Residents", s"/home residents $homeName")

					t"$YELLOW$homeName $setButton $inviteButton $residentsButton $deleteButton"
				}

				val limitText = t"Limit: $limit"
				val newButton = shiftButton(t"${YELLOW}New home", "/home set")

				builder.contents(limitText +: newButton +: homeText: _*)
				builder.sendTo(src)
				CommandResult.builder().successCount(homes.size).build()
			case Left(error) => throw error
		}
	}

	override def commandSpec: CommandSpec = CommandSpec.builder()
		.description(t"Lists all of your current homes")
		.permission(LibPerm.HomeList)
		.executor(this)
		.build()

	override def aliases: Seq[String] = Seq("list", "homes")
}
