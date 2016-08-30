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
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.ConfigValue
import ninja.leaping.configurate.commented.CommentedConfigurationNode

class HomeConfigV1(cfgRoot: CommentedConfigurationNode, default: HomeConfig)(implicit plugin: KatPlugin) extends HomeConfig {

	LogHelper.info("Config with version 1 is old. Updating to version 2")
	override val homeLimitDefault     = ConfigValue(cfgRoot, default.homeLimitDefault)
	override val residentLimitDefault = default.residentLimitDefault
	override val version              = default.version
	override val timeout              = default.version

	override val text = new TextMessages {
		override val homeTeleport     = default.text.homeTeleport
		override val homeDelete       = default.text.homeDelete
		override val homeSet          = default.text.homeSet
		override val homeList         = default.text.homeList
		override val homeLimit        = default.text.homeLimit
		override val inviteSrc        = default.text.inviteSrc
		override val invitePlayer     = default.text.invitePlayer
		override val gotoValid        = default.text.gotoValid
		override val gotoRequestSrc   = default.text.gotoRequestSrc
		override val gotoRequestOwner = default.text.gotoRequestOwner
		override val acceptSuccess    = default.text.acceptSuccess

		override val residentsList           = default.text.residentsList
		override val residentsNone           = default.text.residentsNone
		override val residentsLimit          = default.text.residentsLimit
		override val residentsAddSrc         = default.text.residentsAddSrc
		override val residentsAddPlayer      = default.text.residentsAddPlayer
		override val residentsAddAlready     = default.text.residentsAddAlready
		override val residentsRemoveSrc      = default.text.residentsRemoveSrc
		override val residentsRemovePlayer   = default.text.residentsRemovePlayer
		override val residentsRemoveNotExist = default.text.residentsRemoveNotExist

		override val homeOtherTeleport     = default.text.homeOtherTeleport
		override val homeOtherDelete       = default.text.homeOtherDelete
		override val homeOtherSet          = default.text.homeOtherSet
		override val homeOtherList         = default.text.homeOtherList
		override val homeOtherLimit        = default.text.homeOtherLimit
		override val homeOtherLimitReached = default.text.homeOtherLimitReached
		override val inviteOtherSrc        = default.text.inviteOtherSrc
		override val inviteOtherPlayer     = default.text.inviteOtherPlayer

		override val residentsOtherList           = default.text.residentsOtherList
		override val residentsOtherNone           = default.text.residentsOtherNone
		override val residentsOtherLimit          = default.text.residentsOtherLimit
		override val residentsOtherLimitReached   = default.text.residentsOtherLimitReached
		override val residentsOtherAddSrc         = default.text.residentsOtherAddSrc
		override val residentsOtherAddPlayer      = default.text.residentsOtherAddPlayer
		override val residentsOtherAddAlready     = default.text.residentsOtherAddAlready
		override val residentsOtherRemoveSrc      = default.text.residentsOtherRemoveSrc
		override val residentsOtherRemovePlayer   = default.text.residentsOtherRemovePlayer
		override val residentsOtherRemoveNotExist = default.text.residentsOtherRemoveNotExist

		override val homeNoHomes           = default.text.homeNoHomes
		override val residentsLimitReached = default.text.residentsLimitReached
		override val homeLimitReached      = default.text.homeLimitReached
		override val invalidRequest        = default.text.invalidRequest
		override val acceptRequester       = default.text.acceptRequester
		override val requestOffline        = default.text.requestOffline
		override val homeNotFound          = default.text.homeNotFound
		override val teleportError         = default.text.teleportError
		override val onlyPlayers           = default.text.onlyPlayers
	}
}
