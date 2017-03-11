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

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.service.user.UserStorageService
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.homesweethome.home.{Home, HomeHandler}
import io.github.katrix.homesweethome.lib.{LibCommandKey, LibPerm}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._

class CmdHomeResidents(homeHandler: HomeHandler, parent: CmdHome)(implicit plugin: KatPlugin) extends CommandBase(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
		if(args.hasAny(LibCommandKey.Home)) {
			val data = for {
				player <- playerTypeable.cast(src).toRight(nonPlayerError)
				home   <- args.getOne[(Home, String)](LibCommandKey.Home).toOption.toRight(homeNotFoundError)
			} yield (home._2, home._1.residents, homeHandler.getResidentLimit(player))

			data match {
				case Right((homeName, residents, limit)) =>
					val userStorage = Sponge.getServiceManager.provideUnchecked(classOf[UserStorageService])
					val builder     = Sponge.getServiceManager.provideUnchecked(classOf[PaginationService]).builder()
					builder.title(t"""$YELLOW"$homeName"'s residents""")

					val residentText = {
						if (residents.isEmpty) Seq(t"${YELLOW}No residents")
						else
							residents.sorted
								.map(
									uuid =>
										userStorage
											.get(uuid)
											.toOption
											.map(_.getName)
								)
								.collect { case Some(str) => str }
								.map { residentName =>
									val deleteButton = shiftButton(t"${RED}Delete", s"/home residents remove $residentName $homeName")

									t"$residentName $deleteButton"
								}
					}

					val limitText = t"Limit: $limit"
					val newButton = shiftButton(t"${YELLOW}New resident", s"/home residents add <player> $homeName")

					builder.contents(limitText +: newButton +: residentText: _*)

					builder.sendTo(src)
					CommandResult.builder().successCount(residents.size).build()
				case Left(error) => throw error
			}
		}
		else {
			val data = for {
				player <- playerTypeable.cast(src).toRight(nonPlayerError)
			} yield (player, homeHandler.allHomesForPlayer(player.getUniqueId).mapValues(_.residents), homeHandler.getResidentLimit(player))

			data match {
				case Right((player, residents, limit)) =>
					val userStorage = Sponge.getServiceManager.provideUnchecked(classOf[UserStorageService])
					val builder     = Sponge.getServiceManager.provideUnchecked(classOf[PaginationService]).builder()
					builder.title(t"""$YELLOW${player.getName}'s residents""")

					val residentText = {
						if (residents.isEmpty) Seq(t"${YELLOW}No homes")
						else
							residents.toSeq.sortBy(_._1)
								.map {
									case (homeName, homeResidentsUuids) =>
										val details = shiftButton(t"${YELLOW}Details", s"/home residents $homeName")
										if(homeResidentsUuids.isEmpty) t"$homeName: ${YELLOW}No residents$RESET $details"
										else {
											val homeResidents = homeResidentsUuids.flatMap(userStorage.get(_).toOption.map(_.getName))
											t""""$homeName": $YELLOW${homeResidents.mkString(", ")}$RESET $details"""
										}
								}
					}

					val limitText = t"Limit: $limit"

					builder.contents(limitText +: residentText: _*)

					builder.sendTo(src)
					CommandResult.builder().successCount(residents.values.flatten.size).build()

				case Left(e) => throw e
			}
		}
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .arguments(GenericArguments.optional(new CommandElementHome(LibCommandKey.Home, homeHandler)))
      .description(t"List the residents of a home")
      .permission(LibPerm.HomeResidentsList)
      .executor(this)
      .children(this)
      .build()

  override def aliases: Seq[String] = Seq("residents", "res")

  override def children: Seq[CommandBase] =
    Seq(new CmdHomeResidentsAdd(homeHandler, this), new CmdHomeResidentsLimit(homeHandler, this), new CmdHomeResidentsRemove(homeHandler, this))
}
