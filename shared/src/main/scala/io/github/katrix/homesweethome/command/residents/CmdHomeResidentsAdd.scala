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
package residents

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.home.{Home, HomeHandler}
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibCommonCommandKey

class CmdHomeResidentsAdd(homeHandler: HomeHandler, parent: CmdHomeResidents)(implicit plugin: KatPlugin) extends CommandBase(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val data = for {
      player <- playerTypeable.cast(src).toRight(nonPlayerError)
      target <- args.getOne[Player](LibCommonCommandKey.Player).toOption.toRight(playerNotFoundError)
      home   <- args.getOne[(Home, String)](LibCommandKey.Home).toOption.toRight(homeNotFoundError)
    } yield (player, target, home._1, home._2, home._1.residents.size < homeHandler.getResidentLimit(player))

    data match {
      case Right((player, target, home, homeName, true)) if !home.residents.contains(target.getUniqueId) =>
        val newHome = home.addResident(target.getUniqueId)
        homeHandler.updateHome(player.getUniqueId, homeName, newHome)
        src.sendMessage(t"""${GREEN}Adding ${target.getName} as a resident to "$homeName"""")
        CommandResult.success()
      case Right((_, target, _, homeName, true)) =>
        src.sendMessage(t"""$RED${target.getName} is already a resident of "$homeName"""")
        CommandResult.empty()
      case Right((_, _, _, _, false)) => throw new CommandException(t"${RED}Resident limit reached")
      case Left(error)                => throw error
    }
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.player(LibCommonCommandKey.Player), new CommandElementHome(LibCommandKey.Home, homeHandler))
      .description(t"Add a user as a resident to a home")
      .permission(LibPerm.HomeResidentsAdd)
      .executor(this)
      .build()

  override def aliases: Seq[String] = Seq("add")
}
