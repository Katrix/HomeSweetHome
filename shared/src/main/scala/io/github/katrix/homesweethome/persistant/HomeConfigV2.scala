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

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.persistant.ConfigValue
import ninja.leaping.configurate.commented.CommentedConfigurationNode

class HomeConfigV2(cfgRoot: CommentedConfigurationNode, default: HomeConfig)(implicit plugin: KatPlugin) extends HomeConfig {

	override val homeLimitDefault     = ConfigValue(cfgRoot, default.homeLimitDefault)
	override val residentLimitDefault = ConfigValue(cfgRoot, default.residentLimitDefault)
	override val version              = ConfigValue(cfgRoot, default.version)
	override val timeout              = ConfigValue(cfgRoot, default.timeout)

	override val text = new TextMessages {
		override val homeTeleport     = ConfigValue(cfgRoot, default.text.homeTeleport)
		override val homeDelete       = ConfigValue(cfgRoot, default.text.homeDelete)
		override val homeSet          = ConfigValue(cfgRoot, default.text.homeSet)
		override val homeList         = ConfigValue(cfgRoot, default.text.homeList)
		override val homeLimit        = ConfigValue(cfgRoot, default.text.homeLimit)
		override val inviteSrc        = ConfigValue(cfgRoot, default.text.inviteSrc)
		override val invitePlayer     = ConfigValue(cfgRoot, default.text.invitePlayer)
		override val gotoValid        = ConfigValue(cfgRoot, default.text.gotoValid)
		override val gotoRequestSrc   = ConfigValue(cfgRoot, default.text.gotoRequestSrc)
		override val gotoRequestOwner = ConfigValue(cfgRoot, default.text.gotoRequestOwner)
		override val acceptSuccess    = ConfigValue(cfgRoot, default.text.acceptSuccess)

		override val residentsList           = ConfigValue(cfgRoot, default.text.residentsList)
		override val residentsNone           = ConfigValue(cfgRoot, default.text.residentsNone)
		override val residentsLimit          = ConfigValue(cfgRoot, default.text.residentsLimit)
		override val residentsAddSrc         = ConfigValue(cfgRoot, default.text.residentsAddSrc)
		override val residentsAddPlayer      = ConfigValue(cfgRoot, default.text.residentsAddPlayer)
		override val residentsAddAlready     = ConfigValue(cfgRoot, default.text.residentsAddAlready)
		override val residentsRemoveSrc      = ConfigValue(cfgRoot, default.text.residentsRemoveSrc)
		override val residentsRemovePlayer   = ConfigValue(cfgRoot, default.text.residentsRemovePlayer)
		override val residentsRemoveNotExist = ConfigValue(cfgRoot, default.text.residentsRemoveNotExist)

		override val homeOtherTeleport     = ConfigValue(cfgRoot, default.text.homeOtherTeleport)
		override val homeOtherDelete       = ConfigValue(cfgRoot, default.text.homeOtherDelete)
		override val homeOtherSet          = ConfigValue(cfgRoot, default.text.homeOtherSet)
		override val homeOtherList         = ConfigValue(cfgRoot, default.text.homeOtherList)
		override val homeOtherLimit        = ConfigValue(cfgRoot, default.text.homeOtherLimit)
		override val homeOtherLimitReached = ConfigValue(cfgRoot, default.text.homeOtherLimitReached)
		override val inviteOtherSrc        = ConfigValue(cfgRoot, default.text.inviteOtherSrc)
		override val inviteOtherPlayer     = ConfigValue(cfgRoot, default.text.inviteOtherPlayer)

		override val residentsOtherList           = ConfigValue(cfgRoot, default.text.residentsOtherList)
		override val residentsOtherNone           = ConfigValue(cfgRoot, default.text.residentsOtherNone)
		override val residentsOtherLimit          = ConfigValue(cfgRoot, default.text.residentsOtherLimit)
		override val residentsOtherLimitReached   = ConfigValue(cfgRoot, default.text.residentsOtherLimitReached)
		override val residentsOtherAddSrc         = ConfigValue(cfgRoot, default.text.residentsOtherAddSrc)
		override val residentsOtherAddPlayer      = ConfigValue(cfgRoot, default.text.residentsOtherAddPlayer)
		override val residentsOtherAddAlready     = ConfigValue(cfgRoot, default.text.residentsOtherAddAlready)
		override val residentsOtherRemoveSrc      = ConfigValue(cfgRoot, default.text.residentsOtherRemoveSrc)
		override val residentsOtherRemovePlayer   = ConfigValue(cfgRoot, default.text.residentsOtherRemovePlayer)
		override val residentsOtherRemoveNotExist = ConfigValue(cfgRoot, default.text.residentsOtherRemoveNotExist)

		override val homeNoHomes           = ConfigValue(cfgRoot, default.text.homeNoHomes)
		override val residentsLimitReached = ConfigValue(cfgRoot, default.text.residentsLimitReached)
		override val homeLimitReached      = ConfigValue(cfgRoot, default.text.homeLimitReached)
		override val invalidRequest        = ConfigValue(cfgRoot, default.text.invalidRequest)
		override val acceptRequester       = ConfigValue(cfgRoot, default.text.acceptRequester)
		override val requestOffline        = ConfigValue(cfgRoot, default.text.requestOffline)
		override val homeNotFound          = ConfigValue(cfgRoot, default.text.homeNotFound)
		override val teleportError         = ConfigValue(cfgRoot, default.text.teleportError)
		override val onlyPlayers           = ConfigValue(cfgRoot, default.text.onlyPlayers)
	}
}
