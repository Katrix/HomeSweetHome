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

import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.command.other.CmdHomeOther
import io.github.katrix.homesweethome.command.residents.CmdHomeResidents
import io.github.katrix.homesweethome.home.{Home, HomeHandler}
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._

class CmdHome(homeHandler: HomeHandler)(implicit plugin: KatPlugin) extends CommandBase(None) {

	override def execute(src: CommandSource, args: CommandContext): CommandResult = {
		val data = for {
			player <- playerTypeable.cast(src).toRight(nonPlayerError).right
			home <- args.getOne[(Home, String)](LibCommandKey.Home).toOption.toRight(homeNotFoundError).right
		} yield (player, home._2, home._1)

		data match {
			case Right((player, homeName, home)) if home.teleport(player) =>
				src.sendMessage(t"""${GREEN}Teleported to "$homeName" successfully""")
				CommandResult.success()
			case Right(_) => throw teleportError
			case Left(error) => throw error
		}
	}

	override def commandSpec: CommandSpec = CommandSpec.builder()
		.description(t"Teleports to a home you have set")
		.extendedDescription(t"You can set a home using ${
			Text.builder("/home set <name of home>").onShiftClick(TextActions.insertText("/home set <name of home>"))
		}")
		.permission(LibPerm.HomeTp)
		.arguments(new CommandElementHome(LibCommandKey.Home, homeHandler))
		.executor(this)
		.children(this)
		.build()

	override def children: Seq[CommandBase] = Seq(
		new CmdHomeList(homeHandler, this),
		new CmdHomeSet(homeHandler, this),
		new CmdHomeDelete(homeHandler, this),
		new CmdHomeAccept(homeHandler, this),
		new CmdHomeGoto(homeHandler, this),
		new CmdHomeInvite(homeHandler, this),
		new CmdHomeLimit(homeHandler, this),
		new CmdHomeOther(homeHandler, this),
		new CmdHomeResidents(homeHandler, this)
	)

	override def aliases: Seq[String] = Seq("home")
}
