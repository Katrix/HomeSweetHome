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

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.{Player, User}

import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibCommonCommandKey

class CmdHomeOtherResidentsAdd(homeHandler: HomeHandler, parent: CmdHomeOtherResidents)(implicit plugin: KatPlugin) extends CommandBase(Some(
	parent)) {

	override def execute(src: CommandSource, args: CommandContext): CommandResult = {
		val data = for {
			homeOwner <- args.getOne[User]("homeOwner".text).toOption.toRight(playerNotFoundError).right
			target <- args.getOne[Player](LibCommonCommandKey.Player).toOption.toRight(playerNotFoundError).right
			homeName <- args.getOne[String](LibCommandKey.Home).toOption.toRight(invalidParameterError).right
			home <- homeHandler.specificHome(homeOwner.getUniqueId, homeName).toRight(homeNotFoundError).right
		} yield (homeOwner, target, home, homeName, home.residents.size < homeHandler.getResidentLimit(homeOwner))

		data match {
			case Right((homeOwner, target, home, homeName, true)) if !home.residents.contains(target.getUniqueId) =>
				val newHome = home.addResident(target.getUniqueId)
				homeHandler.updateHome(homeOwner.getUniqueId, homeName, newHome)
				src.sendMessage(s"""Added ${target.getName} as a resident to "$homeName" for ${homeOwner.getName}""".richText.success())
				target.sendMessage(s"""You have been add as a resident to "$homeName" for ${homeOwner.getName}""".richText.info())
				CommandResult.success()
			case Right((homeOwner, target, _, homeName, true)) =>
				src.sendMessage(s"""${target.getName} is already a resident of "$homeName" for ${homeOwner.getName}""".richText.error())
				CommandResult.empty()
			case Right((homeOwner, _, _, _, false)) => throw new CommandException(s"Home resident limit reached for ${homeOwner.getName}".richText.error())
			case Left(error) => throw error
		}
	}

	override def commandSpec: CommandSpec = CommandSpec.builder()
		.arguments(
			GenericArguments.user("homeOwner".text),
			GenericArguments.player(LibCommonCommandKey.Player),
			GenericArguments.remainingJoinedStrings(LibCommandKey.Home))
		.description("Add a user as a resident to a home for another player".text)
		.permission(LibPerm.HomeResidentsAdd)
		.executor(this)
		.build()

	override def aliases: Seq[String] = Seq("add")
}
