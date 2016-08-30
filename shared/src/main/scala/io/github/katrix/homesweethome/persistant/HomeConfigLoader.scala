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
import io.github.katrix.katlib.helper.Implicits.typeToken
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.{ConfigValue, ConfigLoader => AbstractConfigLoader}

class HomeConfigLoader(dir: Path)(implicit plugin: KatPlugin) extends AbstractConfigLoader[HomeConfig](dir) {

	override protected def loadVersionedData(version: String): HomeConfig = version match {
		case "1" => new HomeConfigV1(cfgRoot, default)
		case "2" => new HomeConfigV2(cfgRoot, default)
		case _ =>
			LogHelper.error("Invalid version in config. Loading default")
			default
	}

	override protected final val default: HomeConfig = new HomeConfig {
		override val homeLimitDefault          = ConfigValue(3, "Type = Int\nThe default limit to how many homes someone can have", Seq("home",
			"homeLimit"))
		override val residentLimitDefault      = ConfigValue(2, "Type = Int\nThe default limit to how many residents a home can have",
			Seq("home", "residentLimit"))
		override val version                   = ConfigValue("2", "Please don't change this", versionNode.getPath.map(_.toString))
		override val timeout: ConfigValue[Int] = ConfigValue(60 * 50, "Type = Int\nThe aamount of time in seconds before an invite or request times out",
			Seq("home", "timeout"))

		override val text = new TextMessages {
			override val homeTeleport     = ConfigValue(
				TextTemplate.of(TextColors.GREEN ,"Teleported to \"", arg(HomeName), "\" successfully"),
				"Type = TextTemplate\n The message show on a successful teleport",
				Seq("text", "home", "teleport"))
			override val homeDelete       = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Deleted \n", arg(HomeName), "\" successfully"),
				"Type = TextTemplate\n The message show on a successful delete",
				Seq("text", "home", "delete"))
			override val homeSet          = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Set \"", arg(HomeName), "\" successfully"),
				"Type = TextTemplate\n The message show on a successful new home",
				Seq("text", "home", "set"))
			override val homeList         = ConfigValue(
				???,
				"Type = TextTemplate\n The message show when geeting the home list",
				Seq("text", "home", "list"))
			override val homeLimit        = ConfigValue(
				TextTemplate.of(TextColors.YELLOW, "Your homes are: ", arg(Homes)),
				"Type = TextTemplate\n The message show when geeting the home limit",
				Seq("text", "home", "limit"))
			override val inviteSrc        = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "Invited ", arg(Target), " to ", arg(HomeName)),
				"Type = TextTemplate\n The message show the user when inviting someone",
				Seq("text", "home", "invite", "src"))
			override val invitePlayer     = ConfigValue(
				TextTemplate.of(TextColors.GREEN, "You have been invited to \n", arg(HomeName), "\" by ", arg(Owner)),
				"Type = TextTemplate\n The message show the target when inviting someone",
				Seq("text", "home", "invite", "player"))
			override val gotoValid        = ConfigValue(
				???,
				"Type = TextTemplate\n The going to someone else's home successfully using the goto command",
				Seq("text", "home", "goto", "valid"))
			override val gotoRequestSrc   = ConfigValue(
				???,
				"Type = TextTemplate\n The message show to the user when sending a request",
				Seq("text", "home", "goto", "request", "src"))
			override val gotoRequestOwner = ConfigValue(
				???,
				"Type = TextTemplate\n The message show to the target when sending a request",
				Seq("text", "home", "goto", "request", "Owner"))
			override val acceptSuccess    = ConfigValue(
				???,
				"Type = TextTemplate\n The message show to the user when accepting a valid request",
				Seq("text", "home", "accept", "success"))

			override val residentsList           = ConfigValue(???, ???, Seq("text", "home", "residents", "list"))
			override val residentsNone           = ConfigValue(???, ???, Seq("text", "home", "residents", "list", "none"))
			override val residentsLimit          = ConfigValue(???, ???, Seq("text", "home", "residents", "limit"))
			override val residentsAddSrc         = ConfigValue(???, ???, Seq("text", "home", "residents", "add", "src"))
			override val residentsAddPlayer      = ConfigValue(???, ???, Seq("text", "home", "residents", "add", "player"))
			override val residentsAddAlready     = ConfigValue(???, ???, Seq("text", "home", "residents", "add", "already"))
			override val residentsRemoveSrc      = ConfigValue(???, ???, Seq("text", "home", "residents", "remove", "src"))
			override val residentsRemovePlayer   = ConfigValue(???, ???, Seq("text", "home", "residents", "remove", "player"))
			override val residentsRemoveNotExist = ConfigValue(???, ???, Seq("text", "home", "residents", "remove", "notExist"))

			override val homeOtherTeleport     = ConfigValue(???, ???, Seq("text", "home", "other", "teleport"))
			override val homeOtherDelete       = ConfigValue(???, ???, Seq("text", "home", "other", "delete"))
			override val homeOtherSet          = ConfigValue(???, ???, Seq("text", "home", "other", "set"))
			override val homeOtherList         = ConfigValue(???, ???, Seq("text", "home", "other", "list"))
			override val homeOtherLimit        = ConfigValue(???, ???, Seq("text", "home", "other", "limit"))
			override val homeOtherLimitReached = ConfigValue(???, ???, Seq("text", "home", "other", "limit", "reached"))
			override val inviteOtherSrc        = ConfigValue(???, ???, Seq("text", "home", "other", "invite", "src"))
			override val inviteOtherPlayer     = ConfigValue(???, ???, Seq("text", "home", "other", "invite", "player"))

			override val residentsOtherList           = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "list"))
			override val residentsOtherNone           = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "list", "none"))
			override val residentsOtherLimit          = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "limit"))
			override val residentsOtherLimitReached   = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "limit", "reached"))
			override val residentsOtherAddSrc         = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "add", "src"))
			override val residentsOtherAddPlayer      = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "add", "player"))
			override val residentsOtherAddAlready     = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "add", "already"))
			override val residentsOtherRemoveSrc      = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "remove", "src"))
			override val residentsOtherRemovePlayer   = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "remove", "player"))
			override val residentsOtherRemoveNotExist = ConfigValue(???, ???, Seq("text", "home", "other", "residents", "remove", "notExist"))

			override val homeNoHomes           = ConfigValue(???, ???, Seq("text", "home", "list", "none"))
			override val residentsLimitReached = ConfigValue(???, ???, Seq("text", "home", "residents", "limit", "reached"))
			override val homeLimitReached      = ConfigValue(???, ???, Seq("text", "home", "limit", "reached"))
			override val invalidRequest        = ConfigValue(???, ???, Seq("text", "home", "goto", "request", "invalid"))
			override val acceptRequester       = ConfigValue(???, ???, Seq("text", "home", "accept", "requester"))
			override val requestOffline        = ConfigValue(???, ???, Seq("text", "home", "goto", "request", "offline"))
			override val homeNotFound          = ConfigValue(???, ???, Seq("text", "home", "error", "homeNotFound"))
			override val teleportError         = ConfigValue(???, ???, Seq("text", "home", "error", "teleportError"))
			override val onlyPlayers           = ConfigValue(???, ???, Seq("text", "home", "error", "onlyPlayers"))
		}
	}
}
