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

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.LibPerm
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibCommonCommandKey

class CmdHomeOtherList(homeHandler: HomeHandler, parent: CmdHomeOther)(implicit plugin: KatPlugin) extends CommandBase(Some(parent)) {

	override def execute(src: CommandSource, args: CommandContext): CommandResult = {
		val data = for {
			target <- args.getOne[User](LibCommonCommandKey.Player).toOption.toRight(playerNotFoundError).right
		} yield (target, homeHandler.allHomesForPlayer(target.getUniqueId).keys.toSeq, homeHandler.getHomeLimit(target))

		data match {
			case Right((target, Seq(), _)) =>
				src.sendMessage(t"$YELLOW${target.getName} doesn't have any homes yet")
				CommandResult.empty()
			case Right((target, homes, limit)) =>
				val builder = Sponge.getServiceManager.provideUnchecked(classOf[PaginationService]).builder()
				val homeOwner = target.getName
				builder.title(t"$YELLOW$homeOwner's homes")

				val homeText = homes.sorted.map { homeName =>
					val setButton = shiftButton(t"${YELLOW}Set", s"/home other set $homeOwner $homeName")
					val inviteButton = shiftButton(t"${YELLOW}Invite", s"/home other invite $homeOwner <player> $homeName")
					val deleteButton = shiftButton(t"${RED}Delete", s"/home other delete $homeOwner $homeName")

					val residentsButton = shiftButton(t"${YELLOW}Residents", s"/home other residents $homeOwner $homeName")

					t"$YELLOW$homeName $setButton $inviteButton $residentsButton $deleteButton"
				}

				val limitText = t"Limit: $limit"
				val newButton = shiftButton(t"${YELLOW}New home", s"/home other set $homeOwner")

				builder.contents(limitText +: newButton +: homeText: _*)
				builder.sendTo(src)
				CommandResult.builder().successCount(homes.size).build()
			case Left(error) => throw error
		}
	}

	override def commandSpec: CommandSpec = CommandSpec.builder()
		.description(t"Lists all of the homes of someone else")
		.permission(LibPerm.HomeOtherList)
		.arguments(GenericArguments.player(LibCommonCommandKey.Player))
		.executor(this)
		.build()

	override def aliases: Seq[String] = Seq("list", "homes")
}
