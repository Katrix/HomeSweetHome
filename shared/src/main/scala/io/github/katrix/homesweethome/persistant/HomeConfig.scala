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

	val textHomeTeleport : ConfigValue[TextTemplate] //One arg homeName
	val textHomeDelete   : ConfigValue[TextTemplate] //One arg homeName
	val textHomeSet      : ConfigValue[TextTemplate] //One arg homeName
	val textHomeList     : ConfigValue[TextTemplate] //One arg homes
	val textHomeLimit    : ConfigValue[TextTemplate] //One arg limit
	val textInviteSrc    : ConfigValue[TextTemplate] //Two args target homeName
	val textInvitePlayer : ConfigValue[TextTemplate] //Two args homeName owner
	val textGotoValid    : ConfigValue[TextTemplate] //Two args homeName owner
	val textGotoRequest  : ConfigValue[TextTemplate] //Two args owner homeName
	val textAcceptSuccess: ConfigValue[TextTemplate] //One arg requester

	val textResidentsList: ConfigValue[TextTemplate] //Two args homeName residents
	val textResidentsNone: ConfigValue[TextTemplate] //One arg homeName

	val textHomeOtherTeleport : ConfigValue[TextTemplate] //Two arg homeName owner
	val textHomeOtherDelete   : ConfigValue[TextTemplate] //Two arg homeName owner
	val textHomeOtherSet      : ConfigValue[TextTemplate] //Two arg homeName owner
	val textHomeOtherList     : ConfigValue[TextTemplate] //Two arg owner homes
	val textHomeOtherLimit    : ConfigValue[TextTemplate] //Two arg owner limit
	val textInviteOtherSrc    : ConfigValue[TextTemplate] //Three args target homeName owner
	val textInviteOtherPlayer : ConfigValue[TextTemplate] //Three args homeName owner target

	val textHomeNoHomes     : ConfigValue[Text]
	val textHomeLimitReached: ConfigValue[Text]
	val textInvalidRequest  : ConfigValue[Text]
	val textAcceptRequester : ConfigValue[Text]
	val textHomeNotFound    : ConfigValue[Text]

	override def seq: Seq[ConfigValue[_]] = Seq(homeLimitDefault, residentLimitDefault, version)
}