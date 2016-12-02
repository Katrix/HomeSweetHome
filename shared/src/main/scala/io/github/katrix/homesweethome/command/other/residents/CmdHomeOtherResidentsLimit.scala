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
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.User

import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.LibPerm
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibCommonCommandKey

class CmdHomeOtherResidentsLimit(homeHandler: HomeHandler, parent: CmdHomeOtherResidents)(implicit plugin: KatPlugin) extends CommandBase(Some(
	parent)) {

	override def execute(src: CommandSource, args: CommandContext): CommandResult = args.getOne[User](LibCommonCommandKey.Player).toOption match {
		case Some(player) =>
			val limit = homeHandler.getResidentLimit(player)
			src.sendMessage(t"${player.getName}'s resident limit is: $limit")
			CommandResult.builder().successCount(limit).build()
		case None => throw nonPlayerError
	}

	override def commandSpec: CommandSpec = CommandSpec.builder()
		.arguments(GenericArguments.user(LibCommonCommandKey.Player))
		.description(t"See how many residents another player can have for a home")
		.permission(LibPerm.HomeOtherResidentsLimit)
		.executor(this)
		.build()

	override def aliases: Seq[String] = Seq("limit")
}
