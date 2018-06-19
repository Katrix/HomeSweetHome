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
package net.katsstuff.homesweethome.home

import java.util.UUID

import io.circe._
import cats.syntax.all._
import cats.instances.option._
import net.katstuff.katlib.algebras.Locations

case class Home(x: Double, y: Double, z: Double, yaw: Double, pitch: Double, worldUuid: UUID, residents: Seq[UUID]) {

  def teleport[F[_], Location, Player](player: Player)(implicit locations: Locations[F, Location, Player]): F[Boolean] =
    for {
      optLocation     <- locations.createLocation(x, y, z, yaw, pitch, worldUuid)
      optSafeLocation <- optLocation.traverse(_.makeSafe)
      success         <- optSafeLocation.traverse(locations.teleport(player, _))
    } yield success.exists(identity)

  def addResident(resident: UUID):    Home = copy(residents = resident +: residents)
  def removeResident(resident: UUID): Home = copy(residents = residents.filter(_ != resident))
}
object Home {
  implicit val decoder: Decoder[Home] = generic.semiauto.deriveDecoder
  implicit val encoder: Encoder[Home] = generic.semiauto.deriveEncoder
}
