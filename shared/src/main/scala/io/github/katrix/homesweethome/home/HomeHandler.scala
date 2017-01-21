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
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.api.world.{Location, World}

import com.flowpowered.math.vector.Vector3d
import com.google.common.cache.CacheBuilder

import io.github.katrix.homesweethome.persistant.{HomeConfig, StorageLoader}

/**
	* The HomeHandler is what manages all the homes.
	* @param storage The home storage where everything is saved
	* @param config The config to read from. Pass-by-name to allow it to use a new config if there is one
	*/
abstract class HomeHandler(storage: StorageLoader, config: => HomeConfig) {

	private val homeMap = new mutable.HashMap[UUID, mutable.Map[String, Home]]()
		.withDefault(_ => new mutable.HashMap[String, Home]())

	private var requests: mutable.Map[Player, mutable.Map[UUID, Home]] = new mutable.WeakHashMap[Player, mutable.Map[UUID, Home]]
	private var invites : mutable.Map[Player, mutable.Map[UUID, Home]] = new mutable.WeakHashMap[Player, mutable.Map[UUID, Home]]

	/**
		* Clears the current homes and reloads them from disk.
		*/
	def reloadHomeData(): Unit = {
		homeMap.clear()
		requests.clear()
		invites.clear()

		requests = requests.withDefault(_ => CacheBuilder.newBuilder()
			.expireAfterWrite(config.timeout.value, TimeUnit.SECONDS).build[UUID, Home]().asMap.asScala)
		invites = invites.withDefault(_ => CacheBuilder.newBuilder()
			.expireAfterWrite(config.timeout.value, TimeUnit.SECONDS).build[UUID, Home]().asMap.asScala)

		val toAdd = storage.loadData.map{ case (k, v) => (k, mutable.Map(v.toSeq: _*))}
		homeMap ++= toAdd
	}

	/**
		* Add a home request
		*/
	def addRequest(requester: Player, homeOwner: UUID, home: Home): Unit = requests(requester).put(homeOwner, home)

	/**
		* Removed a home request
		*/
	def removeRequest(requester: Player, homeOwner: UUID): Unit = requests(requester).remove(homeOwner)

	/**
		* Get a home request
		*/
	def getRequest(requester: Player, homeOwner: UUID): Option[Home] = requests(requester).get(homeOwner)

	/**
		* Add a new invite to a specific home for a homeowner
		*/
	def addInvite(target: Player, homeOwner: UUID, home: Home): Unit = {
		println(invites.default(target))
		invites(target).put(homeOwner, home)
	}

	/**
		* Removed an invite
		*/
	def removeInvite(player: Player, homeOwner: UUID): Unit = invites(player).remove(homeOwner)

	/**
		* Check if a player is invited to a specific home
		*/
	def isInvited(target: Player, homeOwner: UUID, home: Home): Boolean = invites(target).get(homeOwner).contains(home)

	/**
		* Gets all the homes for a specific player.
		*
		* @return A map containing all the homes for a specific player.
		* The map itself is a copy.
		*/
	def allHomesForPlayer(uuid: UUID): Map[String, Home] = Map(homeMap(uuid).toSeq: _*)

	/**
		* Gets a specific home for a player.
		*
		* @param uuid UUID of player
		* @param name Name of player
		* @return Home if it was found
		*/
	def specificHome(uuid: UUID, name: String): Option[Home] = homeMap(uuid).get(name)

	/**
		* Check if a player has a home with the specific name.
		*
		* @param uuid UUID of player
		* @param name Name of home
		* @return If home exist
		*/
	def homeExist(uuid: UUID, name: String): Boolean = homeMap(uuid).contains(name)

	/**
		* Creates a new home for a player.
		*
		* @param player The owner of the new home.
		* Also used to get location of new home
		* @param name Name of new home
		*/
	def makeHome(player: Player, name: String): Unit = makeHome(player.getUniqueId, name, player.getLocation, player.getRotation)

	/**
		* Makes a new home for a player with a specific location.
		*
		* @param uuid The UUID of the player
		* @param name The name of the new home
		* @param location The location of the new home
		* @param rotation The rotation of the new home
		*/
	def makeHome(uuid: UUID, name: String, location: Location[World], rotation: Vector3d): Unit = {
		val playerMap = homeMap(uuid)
		playerMap.put(name, new Home(location, rotation))
		homeMap.put(uuid, playerMap) //We put the map back as we could have made a new one

		save()
	}

	/**
		* Deletes a home with the specific name.
		*
		* @param uuid The UUID if the player to delete the home for.
		* @param name The name of the home to delete.
		*/
	def deleteHome(uuid: UUID, name: String): Unit = {
		homeMap(uuid).remove(name)
		save()
	}

	/**
		* Updates an existing home
		*
		* @param homeOwner The owner of the home
		* @param homeName The name of the home to modify
		* @param newHome The new home to use
		*/
	def updateHome(homeOwner: UUID, homeName: String, newHome: Home): Unit = {
		homeMap(homeOwner).put(homeName, newHome)
		save()
	}

	/**
		* The amount of homes a player can have
		*/
	def getHomeLimit(player: Subject): Int

	/**
		* The amount of residents a home for a player can have
		*/
	def getResidentLimit(player: Subject): Int

	private def save(): Unit = {
		val toSave = Map(homeMap.map { case (key, map) => (key, Map(map.toSeq: _*)) }.toSeq: _*)
		storage.saveMap(toSave)
	}
}
