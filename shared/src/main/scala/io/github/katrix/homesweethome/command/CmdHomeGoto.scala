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

import scala.collection.JavaConverters._

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.{Player, User}

import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.homesweethome.persistant.HomeConfig
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibCommonCommandKey

class CmdHomeGoto(homeHandler: HomeHandler, parent: CmdHome)(implicit plugin: KatPlugin, config: HomeConfig) extends CommandBase(Some(parent)) {

	override def execute(src: CommandSource, args: CommandContext): CommandResult = {
		val data = for {
			player <- src.asInstanceOfOpt[Player].toRight(nonPlayerError).right
			homeOwner <- args.getOne[User](LibCommonCommandKey.Player).toOption.toRight(playerNotFoundError).right
			homeName <- args.getOne[String](LibCommandKey.Home).toOption.toRight(invalidParameterError).right
			home <- homeHandler.specificHome(player.getUniqueId, homeName).toRight(homeNotFoundError).right
		} yield {
			val isResident = home.residents.contains(player.getUniqueId)
			val isInvited = homeHandler.isInvited(player, homeOwner.getUniqueId, home) && homeOwner.isOnline
			(player, homeOwner, homeName, home, isResident || isInvited)
		}

		data match {
			case Right((player, homeOwner, homeName, home, true)) if home.teleport(player) =>
				src.sendMessage(config.text.gotoValid.value(Map(config.HomeName -> homeName.text, config.Owner -> homeOwner.getName.text).asJava).build())
				CommandResult.success()
			case Right((_, _, _, _, true)) => throw teleportError
			case Right((player, homeOwner, homeName, home, false)) if homeOwner.isOnline =>
				homeHandler.addRequest(player, homeOwner.getUniqueId, home)
				src.sendMessage(config.text.gotoRequestSrc.value(Map(config.Owner -> homeOwner.getName.text, config.HomeName -> homeName.text).asJava).build())
				homeOwner.getPlayer.get().sendMessage(config.text.gotoRequestOwner.value(Map(config.Target -> player.getName.text,
					config.HomeName -> homeName.text).asJava).build())
				CommandResult.success()
			case Right((_, _, _, _, false)) => throw new CommandException(config.text.requestOffline.value)
			case Left(error) => throw error
		}
	}

	override def commandSpec: CommandSpec = CommandSpec.builder()
		.arguments(GenericArguments.user(LibCommonCommandKey.Player), GenericArguments.remainingJoinedStrings(LibCommandKey.Home))
		.description("Go to another players home if they are allowed to go there.".text)
		.extendedDescription("To be allowed into a home you either need to be a resident, or be invited".text)
		.permission(LibPerm.HomeGoto)
		.executor(this)
		.build()

	override def aliases: Seq[String] = Seq("goto")
}
