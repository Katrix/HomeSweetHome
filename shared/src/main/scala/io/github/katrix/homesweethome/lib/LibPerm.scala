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
package io.github.katrix.homesweethome.lib

object LibPerm {

  final val HomeSweetHome = "homesweethome"
  final val Help          = s"$HomeSweetHome.help"

  final val HomeLimitOption     = s"$HomeSweetHome.homelimit"
  final val ResidentLimitOption = s"$HomeSweetHome.residentlimit"

  final val Home       = s"$HomeSweetHome.home"
  final val HomeTp     = s"$Home.tp"
  final val HomeList   = s"$Home.list"
  final val HomeSet    = s"$Home.set"
  final val HomeDelete = s"$Home.remove"
  final val HomeLimit  = s"$Home.limit"
  final val HomeInvite = s"$Home.invite"
  final val HomeGoto   = s"$Home.goto"
  final val HomeAccept = s"$Home.accept"

  final val HomeResidents       = s"$Home.residents"
  final val HomeResidentsList   = s"$HomeResidents.list"
  final val HomeResidentsAdd    = s"$HomeResidents.add"
  final val HomeResidentsRemove = s"$HomeResidents.remove"
  final val HomeResidentsLimit  = s"$HomeResidents.limit"

  final val HomeOther       = s"$HomeSweetHome.homeother"
  final val HomeOtherTp     = s"$HomeOther.tp"
  final val HomeOtherList   = s"$HomeOther.list"
  final val HomeOtherSet    = s"$HomeOther.set"
  final val HomeOtherDelete = s"$HomeOther.remove"
  final val HomeOtherLimit  = s"$HomeOther.limit"
  final val HomeOtherInvite = s"$HomeOther.invite"

  final val HomeOtherResidents       = s"$HomeOther.residents"
  final val HomeOtherResidentsList   = s"$HomeOtherResidents.list"
  final val HomeOtherResidentsAdd    = s"$HomeOtherResidents.add"
  final val HomeOtherResidentsRemove = s"$HomeOtherResidents.remove"
  final val HomeOtherResidentsLimit  = s"$HomeOtherResidents.limit"
}
