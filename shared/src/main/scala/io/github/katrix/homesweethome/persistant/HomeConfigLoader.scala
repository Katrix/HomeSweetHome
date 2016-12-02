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
package io.github.katrix.homesweethome.persistant

import java.nio.file.Path

import org.spongepowered.api.text.TextTemplate
import org.spongepowered.api.text.TextTemplate.arg
import org.spongepowered.api.text.format.TextColors

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits.{RichString, typeToken}
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.{ConfigValue, ConfigLoader => AbstractConfigLoader}

class HomeConfigLoader(dir: Path)(implicit plugin: KatPlugin) extends AbstractConfigLoader[HomeConfig](dir, identity) {

	override def loadData: HomeConfig = {
		val loaded = cfgRoot.getNode("version").getString("2") match {
			case "1" =>
				cfgRoot.removeChild("text")
				new HomeConfigV1(cfgRoot, default)
			case "2" =>	 new HomeConfigV1(cfgRoot, default)
		}

		saveData(loaded)
		loaded
	}

	override protected final val default: HomeConfig = new HomeConfig {
		override val homeLimitDefault          = ConfigValue(3, "Type = Int\nThe default limit to how many homes someone can have", Seq("home",
			"homeLimit"))
		override val residentLimitDefault      = ConfigValue(2, "Type = Int\nThe default limit to how many residents a home can have",
			Seq("home", "residentLimit"))
		override val version                   = ConfigValue("2", "Please don't change this", Seq("version"))
		override val timeout                   = ConfigValue(60 * 5, "Type = Int\nThe amount of time in seconds before an invite or request times out",
			Seq("home", "timeout"))

		override val text = new TextMessages {
			override val homeTeleport     = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Teleported to \"", arg(HomeName), "\" successfully"),
				"Type = TextTemplate\nThe message shown on a successful teleport",
				Seq("text", "home", "teleport"))
			override val homeDelete       = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Deleted \"", arg(HomeName), "\" successfully"),
				"Type = TextTemplate\nThe message shown on a successful delete",
				Seq("text", "home", "delete"))
			override val homeSet          = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Set \"", arg(HomeName), "\" successfully"),
				"Type = TextTemplate\nThe message shown on a successful new home",
				Seq("text", "home", "set"))
			override val homeList         = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "Your homes are: ", arg(Homes)),
				"Type = TextTemplate\nThe message shown when getting the home list",
				Seq("text", "home", "list"))
			override val homeLimit        = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "Your home limit is: ", arg(Limit)),
				"Type = TextTemplate\nThe message shown when getting the home limit",
				Seq("text", "home", "limit"))
			override val inviteSrc        = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Invited ", arg(Target), " to \"", arg(HomeName), "\""),
				"Type = TextTemplate\nThe message shown to the user when inviting someone",
				Seq("text", "home", "invite", "src"))
			override val invitePlayer     = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "You have been invited to \"", arg(HomeName), "\" by ", arg(Owner)),
				"Type = TextTemplate\nThe message shown to the target when inviting someone",
				Seq("text", "home", "invite", "player"))
			override val gotoValid        = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Teleported to \"", arg(HomeName), "\" for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown when going to someone else's home successfully using the goto command",
				Seq("text", "home", "goto", "valid"))
			override val gotoRequestSrc   = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Sent home request to", arg(Owner), " for \"", arg(HomeName), "\""),
				"Type = TextTemplate\nThe message shown to the user when sending a request",
				Seq("text", "home", "goto", "request", "src"))
			override val gotoRequestOwner = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, arg(Target), " has requested a to be teleported to \"", arg(HomeName), "\"\nType /home accept ", arg(
					Target), " to accept"),
				"Type = TextTemplate\nThe message shown to the target when sending a request",
				Seq("text", "home", "goto", "request", "Owner"))
			override val acceptSuccess    = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Teleported ", arg(Requester), " to their requested home"),
				"Type = TextTemplate\nThe message shown to the user when accepting a valid request",
				Seq("text", "home", "accept", "success"))

			override val residentsList           = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "The residents of \"", arg(HomeName), "\" are: ", arg(Residents)),
				"Type = TextTemplate\nThe message shown when getting the residents list",
				Seq("text", "home", "residents", "list"))
			override val residentsNone           = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "\"", arg(HomeName), "\" doesn't have any residents yet"),
				"Type = TextTemplate\nThe message shown when getting an empty residents list",
				Seq("text", "home", "residents", "list", "none"))
			override val residentsLimit          = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "Your residents limit is: ", arg(Limit)),
				"Type = TextTemplate\nThe message shown when getting the residents limit",
				Seq("text", "home", "residents", "limit"))
			override val residentsAddSrc         = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Added ", arg(Target), " as a resident to \"", arg(HomeName), "\""),
				"Type = TextTemplate\nThe message shown to the user when adding a resident",
				Seq("text", "home", "residents", "add", "src"))
			override val residentsAddPlayer      = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "You have been added as a resident to \"", arg(HomeName), "\" for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown to the target when adding a resident",
				Seq("text", "home", "residents", "add", "player"))
			override val residentsAddAlready     = ConfigValue(
				TextTemplate.of(TextColors.RED, arg(Target), " is already a resident of \"", arg(HomeName), "\""),
				"Type = TextTemplate\nThe message shown when adding a player as a resident to a home, and the player is already a resident",
				Seq("text", "home", "residents", "add", "already"))
			override val residentsRemoveSrc      = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Removed ", arg(Target), " as a resident from \"", arg(HomeName), "\""),
				"Type = TextTemplate\nThe message shown to the user when removing a resident",
				Seq("text", "home", "residents", "remove", "src"))
			override val residentsRemovePlayer   = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "You have been removed as a resident from \"", arg(HomeName), "\" for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown to the target when removing a resident",
				Seq("text", "home", "residents", "remove", "player"))
			override val residentsRemoveNotExist = ConfigValue(
				TextTemplate.of(TextColors.RED, arg(Target), " is not a resident of \"", arg(HomeName), "\""),
				"Type = TextTemplate\nThe message shown when removing a player as a resident, but the player isn't a resident",
				Seq("text", "home", "residents", "remove", "notExist"))

			override val homeOtherTeleport     = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Teleported to \"", arg(HomeName), "\" for ", arg(Owner), " successfully"),
				"Type = TextTemplate\nThe message shown on a successful teleport to another players home",
				Seq("text", "home", "other", "teleport"))
			override val homeOtherDelete       = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Deleted \"", arg(HomeName), "\" for ", arg(Owner), " successfully"),
				"Type = TextTemplate\nThe message shown on a successful delete for another players",
				Seq("text", "home", "other", "delete"))
			override val homeOtherSet          = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Set \"", arg(HomeName), "\" for ", arg(Owner), " successfully"),
				"Type = TextTemplate\nThe message shown on a successful new home for another player",
				Seq("text", "home", "other", "set"))
			override val homeOtherList         = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, arg(Owner), "'s homes are: ", arg(Homes)),
				"Type = TextTemplate\nThe message shown when getting the home list for another player",
				Seq("text", "home", "other", "list"))
			override val homeOtherListNone     = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, arg(Owner), " doesn't have any homes yet"),
				"Type = TextTemplate\nThe message shown when getting an empty home list for another player",
				Seq("text", "home", "other", "list"))
			override val homeOtherLimit        = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, arg(Owner), "'s home limit is: ", arg(Limit)),
				"Type = TextTemplate\nThe message shown when getting the home limit for another player",
				Seq("text", "home", "other", "limit"))
			override val homeOtherLimitReached = ConfigValue(
				TextTemplate.of("Home limit reached for ", arg(Owner)),
				"Type = Text\nThe message shown reaching the home limit for another player",
				Seq("text", "home", "other", "limit", "reached"))
			override val inviteOtherSrc        = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Invited ", arg(Target), " to \"", arg(HomeName), "\" for ", arg(Owner), " successfully"),
				"Type = TextTemplate\nThe message shown the user when inviting someone for another player",
				Seq("text", "home", "other", "invite", "src"))
			override val inviteOtherPlayer     = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "You have been invited to \"", arg(HomeName), "\" for ", arg(Target), " by ", arg(Owner)),
				"Type = TextTemplate\nThe message shown the target when inviting someone for another player",
				Seq("text", "home", "other", "invite", "player"))

			override val residentsOtherList           = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "The residents of \"", arg(HomeName), "\" for ", arg(Owner), " are: ", arg(Residents)),
				"Type = TextTemplate\nThe message shown when getting the residents list for another player",
				Seq("text", "home", "other", "residents", "list"))
			override val residentsOtherNone           = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "\"", arg(HomeName), "\" for ", arg(Owner), " doesn't have any residents yet"),
				"Type = TextTemplate\nThe message shown when getting an empty residents list for another player",
				Seq("text", "home", "other", "residents", "list", "none"))
			override val residentsOtherLimit          = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, arg(Owner), "'s residents limit is: ", arg(Limit)),
				"Type = TextTemplate\nThe message shown when getting the residents limit for another player",
				Seq("text", "home", "other", "residents", "limit"))
			override val residentsOtherLimitReached   = ConfigValue(
				TextTemplate.of("Residents limit reached for ", arg(Owner)),
				"Type = Text\nThe message shown reaching the residents limit for another player",
				Seq("text", "home", "other", "residents", "limit", "reached"))
			override val residentsOtherAddSrc         = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Added ", arg(Target), " as a resident to \"", arg(HomeName), "\" for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown to the user when adding a resident for another player",
				Seq("text", "home", "other", "residents", "add", "src"))
			override val residentsOtherAddPlayer      = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "You have been added as a resident to \"", arg(HomeName), "\" for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown to the target when adding a resident for another player",
				Seq("text", "home", "other", "residents", "add", "player"))
			override val residentsOtherAddAlready     = ConfigValue(
				TextTemplate.of(TextColors.RED, arg(Target), " is already a resident in \"", arg(HomeName), "\" for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown when adding a player as a resident to a home for another player, and the player is already a resident",
				Seq("text", "home", "other", "residents", "add", "already"))
			override val residentsOtherRemoveSrc      = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Removed ", arg(Target), " as a resident from \"", arg(HomeName), "\n for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown to the user when removing a resident for another player",
				Seq("text", "home", "other", "residents", "remove", "src"))
			override val residentsOtherRemovePlayer   = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "You have been removed as a resident from \"", arg(HomeName), "\" for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown to the target when removing a resident for another player",
				Seq("text", "home", "other", "residents", "remove", "player"))
			override val residentsOtherRemoveNotExist = ConfigValue(
				TextTemplate.of(TextColors.RED, arg(Target), " is not a resident of \"", arg(HomeName), "\" for ", arg(Owner)),
				"Type = TextTemplate\nThe message shown when removing a player as a resident for another player, but the player isn't a resident",
				Seq("text", "home", "other", "residents", "remove", "notExist"))

			override val homeNoHomes           = ConfigValue(
				"You don't have any homes".richText.info(),
				"Type = Text\nThe message shown listing homes, but there are not homes made yet",
				Seq("text", "home", "list", "none"))
			override val residentsLimitReached = ConfigValue(
				"Resident limit reached".richText.error(),
				"Type = Text\nThe message shown reaching the residents limit",
				Seq("text", "home", "residents", "limit", "reached"))
			override val homeLimitReached      = ConfigValue(
				"Home limit reached".richText.error(),
				"Type = Text\nThe message shown reaching the home limit",
				Seq("text", "home", "limit", "reached"))
			override val invalidRequest        = ConfigValue(
				"That player has not sent a home request".text,
				"Type = Text\nThe message shown if there is no home request for someone",
				Seq("text", "home", "goto", "request", "invalid"))
			override val acceptRequester       = ConfigValue(
				"Teleported you to your requested home".richText.info(),
				"Type = Text\nThe message shown to the requester when the owner accepts a request",
				Seq("text", "home", "accept", "requester"))
			override val requestOffline        = ConfigValue(
				"The player you tried to send a home request to is offline".richText.error(),
				"Type = Text\nThe message shown if trying to send a request of an offline user",
				Seq("text", "home", "goto", "request", "offline"))
			override val homeNotFound          = ConfigValue(
				"A home with that name was not found".richText.error(),
				"Type = Text\nThe message shown if the home with the given name is not found",
				Seq("text", "home", "error", "homeNotFound"))
			override val teleportError         = ConfigValue(
				"A teleport error occurred, is the home in a safe place, and does the world exist".richText.error(),
				"Type = Text\nThe message shown if a teleport error happens",
				Seq("text", "home", "error", "teleportError"))
			override val onlyPlayers           = ConfigValue(
				"This command can only be used by players".richText.error(),
				"Type = Text\nThe message shown if a non player tried to use a player only command",
				Seq("text", "home", "error", "onlyPlayers"))
		}
	}
}
