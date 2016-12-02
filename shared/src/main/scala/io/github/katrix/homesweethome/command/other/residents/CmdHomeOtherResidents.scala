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

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.service.user.UserStorageService

import io.github.katrix.homesweethome.command.other.CmdHomeOther
import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibCommonCommandKey
import org.spongepowered.api.text.format.TextColors._

class CmdHomeOtherResidents(homeHandler: HomeHandler, parent: CmdHomeOther)(implicit plugin: KatPlugin) extends CommandBase(Some(parent)) {

	override def execute(src: CommandSource, args: CommandContext): CommandResult = {
		val data = for {
			player <- args.getOne[User](LibCommonCommandKey.Player).toOption.toRight(nonPlayerError).right
			homeName <- args.getOne[String](LibCommandKey.Home).toOption.toRight(invalidParameterError).right
			home <- homeHandler.specificHome(player.getUniqueId, homeName).toRight(homeNotFoundError).right
		} yield (player, homeName, home.residents)

		data match {
			case Right((player, homeName, Seq())) =>
				src.sendMessage(t""""$YELLOW$homeName" for ${player.getName} doesn't have any residents""")
				CommandResult.empty()
			case Right((player, homeName, residents)) =>
				val userStorage = Sponge.getServiceManager.provideUnchecked[UserStorageService]

				val residentList = residents.sorted
					.map(uuid => userStorage.get(uuid).toOption
						.map(_.getName))
					.collect { case Some(str) => str }
					.mkString(", ")
				src.sendMessage(t"""${YELLOW}The residents of "$homeName" for ${player.getName} are: $residentList""")
				CommandResult.builder().successCount(residents.size).build()
			case Left(error) => throw error
		}
	}

	override def commandSpec: CommandSpec = CommandSpec.builder()
		.arguments(GenericArguments.user(LibCommonCommandKey.Player), GenericArguments.remainingJoinedStrings(LibCommandKey.Home))
		.description(t"List the residents of a home for another player")
		.permission(LibPerm.HomeOtherResidentsList)
		.executor(this)
		.children(this)
		.build()

	override def aliases: Seq[String] = Seq("residents")

	override def children: Seq[CommandBase] = Seq(
		new CmdHomeOtherResidentsLimit(homeHandler, this),
		new CmdHomeOtherResidentsRemove(homeHandler, this),
		new CmdHomeOtherResidentsAdd(homeHandler, this)
	)
}
