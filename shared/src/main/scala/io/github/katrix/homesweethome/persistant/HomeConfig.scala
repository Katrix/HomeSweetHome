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

import org.spongepowered.api.text.{Text, TextTemplate}

import io.github.katrix.katlib.persistant.{Config, ConfigValue}

abstract class HomeConfig extends Config {

	final val HomeName  = "homeName"
	final val Homes     = "homes"
	final val Limit     = "limit"
	final val Target    = "target"
	final val Owner     = "owner"
	final val Requester = "requester"
	final val Residents = "residents"

	val homeLimitDefault    : ConfigValue[Int]
	val residentLimitDefault: ConfigValue[Int]
	val timeout             : ConfigValue[Int]

	val text: TextMessages
	trait TextMessages {
		val homeTeleport    : ConfigValue[TextTemplate] //One arg homeName
		val homeDelete      : ConfigValue[TextTemplate] //One arg homeName
		val homeSet         : ConfigValue[TextTemplate] //One arg homeName
		val homeList        : ConfigValue[TextTemplate] //One arg homes
		val homeLimit       : ConfigValue[TextTemplate] //One arg limit
		val inviteSrc       : ConfigValue[TextTemplate] //Two args target homeName
		val invitePlayer    : ConfigValue[TextTemplate] //Two args homeName owner
		val gotoValid       : ConfigValue[TextTemplate] //Two args homeName owner
		val gotoRequestSrc  : ConfigValue[TextTemplate] //Two args owner homeName
		val gotoRequestOwner: ConfigValue[TextTemplate] //Two args target homeName
		val acceptSuccess   : ConfigValue[TextTemplate] //One arg requester

		val residentsList          : ConfigValue[TextTemplate] //Two args homeName residents
		val residentsNone          : ConfigValue[TextTemplate] //One arg homeName
		val residentsLimit         : ConfigValue[TextTemplate] //One arg limit
		val residentsAddSrc        : ConfigValue[TextTemplate] //Two args target homeName
		val residentsAddPlayer     : ConfigValue[TextTemplate] //Two args homeName owner
		val residentsAddAlready    : ConfigValue[TextTemplate] //Two args target homeName
		val residentsRemoveSrc     : ConfigValue[TextTemplate] //Two args target homeName
		val residentsRemovePlayer  : ConfigValue[TextTemplate] //Two args homeName owner
		val residentsRemoveNotExist: ConfigValue[TextTemplate] //Two args target homeName

		val homeOtherTeleport    : ConfigValue[TextTemplate] //Two arg homeName owner
		val homeOtherDelete      : ConfigValue[TextTemplate] //Two arg homeName owner
		val homeOtherSet         : ConfigValue[TextTemplate] //Two arg homeName owner
		val homeOtherLimitReached: ConfigValue[TextTemplate] //One arg owner
		val homeOtherList        : ConfigValue[TextTemplate] //Two arg owner homes
		val homeOtherLimit       : ConfigValue[TextTemplate] //Two arg owner limit
		val inviteOtherSrc       : ConfigValue[TextTemplate] //Three args target homeName owner
		val inviteOtherPlayer    : ConfigValue[TextTemplate] //Three args homeName owner target

		val residentsOtherList          : ConfigValue[TextTemplate] //Three args homeName owner residents
		val residentsOtherNone          : ConfigValue[TextTemplate] //Two args homeName owner
		val residentsOtherLimit         : ConfigValue[TextTemplate] //Two args target limit
		val residentsOtherLimitReached  : ConfigValue[TextTemplate] //One arg owner
		val residentsOtherAddSrc        : ConfigValue[TextTemplate] //Three args target homeName owner
		val residentsOtherAddPlayer     : ConfigValue[TextTemplate] //Two args homeName owner
		val residentsOtherAddAlready    : ConfigValue[TextTemplate] //Three args target homeName owner
		val residentsOtherRemoveSrc     : ConfigValue[TextTemplate] //Three args target homeName owner
		val residentsOtherRemovePlayer  : ConfigValue[TextTemplate] //Two args homeName owner
		val residentsOtherRemoveNotExist: ConfigValue[TextTemplate] //Three args target homeName owner

		val homeNoHomes          : ConfigValue[Text]
		val residentsLimitReached: ConfigValue[Text]
		val homeLimitReached     : ConfigValue[Text]
		val invalidRequest       : ConfigValue[Text]
		val acceptRequester      : ConfigValue[Text]
		val requestOffline       : ConfigValue[Text]
		val homeNotFound         : ConfigValue[Text]
		val teleportError        : ConfigValue[Text]
		val onlyPlayers          : ConfigValue[Text]
	}

	override def seq: Seq[ConfigValue[_]] = Seq(
		version,
		homeLimitDefault,
		residentLimitDefault,
		timeout,

		text.homeTeleport,
		text.homeDelete,
		text.homeSet,
		text.homeList,
		text.homeLimit,
		text.inviteSrc,
		text.invitePlayer,
		text.gotoValid,
		text.gotoRequestSrc,
		text.gotoRequestOwner,
		text.acceptSuccess,

		text.residentsList,
		text.residentsNone,
		text.residentsLimit,
		text.residentsAddSrc,
		text.residentsAddPlayer,
		text.residentsAddAlready,
		text.residentsRemoveSrc,
		text.residentsRemovePlayer,
		text.residentsRemoveNotExist,

		text.homeOtherTeleport,
		text.homeOtherDelete,
		text.homeOtherSet,
		text.homeOtherList,
		text.homeOtherLimit,
		text.inviteOtherSrc,
		text.inviteOtherPlayer,

		text.residentsOtherList,
		text.residentsOtherNone,
		text.residentsOtherLimit,
		text.residentsOtherLimitReached,
		text.residentsOtherAddSrc,
		text.residentsOtherAddPlayer,
		text.residentsOtherAddAlready,
		text.residentsOtherRemoveSrc,
		text.residentsOtherRemovePlayer,
		text.residentsOtherRemoveNotExist,

		text.homeNoHomes,
		text.residentsLimitReached,
		text.homeLimitReached,
		text.invalidRequest,
		text.acceptRequester,
		text.requestOffline,
		text.homeNotFound,
		text.teleportError,
		text.onlyPlayers
	)
}