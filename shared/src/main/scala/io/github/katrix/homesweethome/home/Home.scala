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
package io.github.katrix.homesweethome.home

import java.util.UUID

import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.{Location, World}

import com.flowpowered.math.vector.Vector3d

import io.github.katrix.katlib.helper.Implicits.RichOptional

case class Home(x: Double, y: Double, z: Double, yaw: Double, pitch: Double, worldUuid: UUID, residents: Seq[UUID]) {

	def this(location: Location[World], rotation: Vector3d) {
		this(location.getX, location.getY, location.getZ, rotation.getX, rotation.getY, location.getExtent.getUniqueId, Seq())
	}

	def world: Option[World] = Sponge.getServer.getWorld(worldUuid).toOption
	def location: Option[Location[World]] = world.map(new Location[World](_, x, y, z))
	def rotation: Vector3d = new Vector3d(yaw, pitch, 0)

	def teleport(player: Player): Boolean = location.exists(loc => player.setLocationAndRotationSafely(loc, rotation))

	def addResident(resident: UUID): Home = copy(residents = resident +: residents)
	def removeResident(resident: UUID): Home = copy(residents = residents.filter(_ != resident))
}
