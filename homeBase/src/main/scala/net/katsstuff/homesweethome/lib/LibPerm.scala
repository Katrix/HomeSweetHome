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
package net.katsstuff.homesweethome.lib

object LibPerm {

  final val Root   = "homesweethome"
  final val Reload = s"$Root.reload"

  final val HomeLimitOption     = "homeLimit"
  final val ResidentLimitOption = "residentLimit"

  final val Home       = s"$Root.home"
  final val HomeDelete = s"$Home.remove"
  final val HomeList   = s"$Home.list"
  final val HomeSet    = s"$Home.set"
  val HomeSetWorld: String => String = worldName => s"$HomeSet.$worldName"
  final val HomeLimit  = s"$Home.limit"
  final val HomeInvite = s"$Home.invite"
  final val HomeAccept = s"$Home.accept"
  final val HomeGoto   = s"$Home.goto"

  final val HomeResident       = s"$Home.residents"
  final val HomeResidentAdd    = s"$HomeResident.add"
  final val HomeResidentRemove = s"$HomeResident.remove"
  final val HomeResidentLimit  = s"$HomeResident.limit"

  final val HomeOther = s"$Home.other"
}
