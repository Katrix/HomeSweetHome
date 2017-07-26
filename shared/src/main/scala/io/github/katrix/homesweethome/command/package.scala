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
package io.github.katrix.homesweethome

import java.util.Locale

import org.spongepowered.api.command.CommandException
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions

import io.github.katrix.katlib.helper.Implicits._
import shapeless.Typeable

package object command {

  val playerTypeable: Typeable[Player] = Typeable[Player]

  def homeNotFoundError(implicit locale: Locale): CommandException = new CommandException(HSHResource.getText("command.error.homeNotFound"))
  def teleportError(implicit locale: Locale): CommandException = new CommandException(HSHResource.getText("command.error.teleportError"))

  def shiftButton(button: Text, text: String): Text = t"[$button]".toBuilder.onClick(TextActions.suggestCommand(text)).build()
}
