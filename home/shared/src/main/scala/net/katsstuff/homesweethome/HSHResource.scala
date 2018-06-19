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
package net.katsstuff.homesweethome

import java.util.{Locale, ResourceBundle}

import org.jetbrains.annotations.PropertyKey
import org.spongepowered.api.text.Text

import net.katsstuff.katlib.i18n.Resource

object HSHResource extends Resource {
  final val ResourceLocation = "assets.homesweethome.lang"

  override def getBundle(implicit locale: Locale): ResourceBundle = ResourceBundle.getBundle(ResourceLocation, locale)

  override def get(@PropertyKey(resourceBundle = ResourceLocation) key: String)(implicit locale: Locale): String =
    super.get(key)

  override def get(@PropertyKey(resourceBundle = ResourceLocation) key: String, params: Map[String, String])(
      implicit locale: Locale
  ): String =
    super.get(key, params)
  override def get(@PropertyKey(resourceBundle = ResourceLocation) key: String, params: (String, String)*)(
      implicit locale: Locale
  ): String =
    super.get(key, params: _*)

  override def getText(@PropertyKey(resourceBundle = ResourceLocation) key: String)(implicit locale: Locale): Text =
    super.getText(key)

  override def getText(@PropertyKey(resourceBundle = ResourceLocation) key: String, params: Map[String, AnyRef])(
      implicit locale: Locale
  ): Text =
    super.getText(key, params)
  override def getText(@PropertyKey(resourceBundle = ResourceLocation) key: String, params: (String, AnyRef)*)(
      implicit locale: Locale
  ): Text =
    super.getText(key, params: _*)
}
