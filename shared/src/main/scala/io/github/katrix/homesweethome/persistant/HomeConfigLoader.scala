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

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits.typeToken
import io.github.katrix.katlib.persistant.{ConfigValue, ConfigLoader => AbstractConfigLoader}

class HomeConfigLoader(dir: Path)(implicit plugin: KatPlugin) extends AbstractConfigLoader[HomeConfig](dir, identity) {

	override def loadData: HomeConfig = {
		val loaded = cfgRoot.getNode("version").getString("2") match {
			case "1" =>
				cfgRoot.removeChild("text")
				new HomeConfigV1(cfgRoot, default)
			case "2" => new HomeConfigV1(cfgRoot, default)
		}

		saveData(loaded)
		loaded
	}

	val default: HomeConfig = new HomeConfig {
		override val homeLimitDefault     = ConfigValue(3, "Type = Int\nThe default limit to how many homes someone can have", Seq("home", "homeLimit"))
		override val residentLimitDefault = ConfigValue(2, "Type = Int\nThe default limit to how many residents a home can have", Seq("home",
			"residentLimit"))
		override val version              = ConfigValue("2", "Please don't change this", Seq("version"))
		override val timeout              = ConfigValue(60 * 5, "Type = Int\nThe amount of time in seconds before an invite or request times out", Seq(
			"home", "timeout"))
	}
}
