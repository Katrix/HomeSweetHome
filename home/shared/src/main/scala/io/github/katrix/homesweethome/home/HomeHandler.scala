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

import java.util.{Locale, UUID}
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.{Location, World}

import com.flowpowered.math.vector.Vector3d
import com.google.common.cache.CacheBuilder

import io.github.katrix.homesweethome.lib.LibPerm
import io.github.katrix.homesweethome.{HSHResource, NestedMap}
import io.github.katrix.homesweethome.persistant.{HomeConfig, StorageLoader}
import io.github.katrix.katlib.i18n.Localized

/**
	* The HomeHandler is what manages all the homes.
	* @param storage The home storage where everything is saved
	* @param config The config to read from. Pass-by-name to allow it to use a new config if there is one
	*/
abstract class HomeHandler(storage: StorageLoader, config: => HomeConfig) {

  private val homeMap: NestedMap[UUID, String, Home] = NestedMap(mutable.HashMap.empty, () => mutable.HashMap.empty)

  private val requests: NestedMap[Player, UUID, Home] = NestedMap(mutable.HashMap.empty, () => createInvitesRequests)
  private val invites:  NestedMap[Player, UUID, Home] = NestedMap(mutable.HashMap.empty, () => createInvitesRequests)

  /**
		* Clears the current homes and reloads them from disk.
		*/
  def reloadHomeData(): Unit = {
    homeMap.clear()
    requests.clear()
    invites.clear()

    val toAdd = for {
      (uuid, innerMap) <- storage.loadData
      (name, home)     <- innerMap
    } yield (uuid, name, home)

    homeMap ++= toAdd
  }

  private def createInvitesRequests[A <: AnyRef, B <: AnyRef]: mutable.Map[A, B] =
    CacheBuilder
      .newBuilder()
      .expireAfterWrite(config.timeout.value, TimeUnit.SECONDS)
      .build[A, B]
      .asMap
      .asScala

  /**
		* Add a home request
		*/
  def addRequest(requester: Player, homeOwner: UUID, home: Home): Unit = requests.put(requester, homeOwner, home)

  /**
		* Removed a home request
		*/
  def removeRequest(requester: Player, homeOwner: UUID): Unit = requests.remove(requester, homeOwner)

  /**
		* Get a home request
		*/
  def getRequest(requester: Player, homeOwner: UUID): Option[Home] = requests.get(requester, homeOwner)

  /**
		* Add a new invite to a specific home for a homeowner
		*/
  def addInvite(target: Player, homeOwner: UUID, home: Home): Unit = invites.put(target, homeOwner, home)

  /**
		* Removed an invite
		*/
  def removeInvite(player: Player, homeOwner: UUID): Unit = invites.remove(player, homeOwner)

  /**
		* Check if a player is invited to a specific home
		*/
  def isInvited(target: Player, homeOwner: UUID, home: Home): Boolean = invites.get(target, homeOwner).contains(home)

  /**
		* Gets all the homes for a specific player.
		*
		* @return A map containing all the homes for a specific player.
		* The map itself is a copy.
		*/
  def allHomesForPlayer(uuid: UUID): Map[String, Home] = homeMap.getAll(uuid)

  /**
		* Gets a specific home for a player.
		*
		* @param uuid UUID of player
		* @param name Name of player
		* @return Home if it was found
		*/
  def specificHome(uuid: UUID, name: String): Option[Home] = homeMap.get(uuid, name)

  /**
		* Check if a player has a home with the specific name.
		*
		* @param uuid UUID of player
		* @param name Name of home
		* @return If home exist
		*/
  def homeExist(uuid: UUID, name: String): Boolean = homeMap.contains(uuid, name)

  /**
		* Creates a new home for a player.
		*
		* @param player The owner of the new home.
		* Also used to get location of new home
		* @param name Name of new home
		*/
  def makeHome(player: Player, name: String): Unit = Localized(player) { implicit locale =>
    makeHome(player.getUniqueId, name, player.getLocation, player.getRotation)
  }

  /**
		* Makes a new home for a player with a specific location.
		*
		* @param uuid The UUID of the player
		* @param name The name of the new home
		* @param location The location of the new home
		* @param rotation The rotation of the new home
		*/
  def makeHome(uuid: UUID, name: String, location: Location[World], rotation: Vector3d): Unit = {
    homeMap.put(uuid, name, new Home(location, rotation))
    save()
  }

  def canCreateHome(player: Player, name: String): Either[Text, Unit] = Localized(player) { implicit locale =>
    canCreateHome(player.getUniqueId, name, player, player.getLocation)
  }

  def canCreateHome(uuid: UUID, name: String, subject: Subject, location: Location[World])(
      implicit locale: Locale
  ): Either[Text, Unit] = {
    for {
      _ <- Right(())
      _ <- Either.cond(
        homeExist(uuid, name) || hasReachedHomeLimit(uuid, subject),
        (),
        HSHResource.getText("command.error.homeLimitReached")
      )
      _ <- canMakeHomeAtLocation(subject, location)
    } yield ()
  }

  /**
    * Check if a player has reached his home limit.
    *
    * @param uuid The uuid of the player
    * @param subject The player
    */
  def hasReachedHomeLimit(uuid: UUID, subject: Subject): Boolean =
    allHomesForPlayer(uuid).size < getHomeLimit(subject)

  /**
    * Check if a home can be placed at a specific location
    * @param subject The player to check for
    * @param location The location to check
    * @return Left with error message if it's an invalid location, else right
    */
  def canMakeHomeAtLocation(subject: Subject, location: Location[World])(
      implicit locale: Locale
  ): Either[Text, Unit] = {
    val isSafe = Sponge.getGame.getTeleportHelper.getSafeLocation(location).isPresent

    for {
      _     <- Either.cond(isSafe, (), HSHResource.getText("command.error.locationNotSafe"))
      world <- Option(location.getExtent).toRight(HSHResource.getText("command.error.worldNotFound"))
      _ <- Either.cond(
        subject.hasPermission(LibPerm.HomeSetWorld(world.getName)),
        (),
        HSHResource.getText("command.error.notAllowedWorld")
      )
    } yield ()
  }

  /**
		* Deletes a home with the specific name.
		*
		* @param uuid The UUID if the player to delete the home for.
		* @param name The name of the home to delete.
		*/
  def deleteHome(uuid: UUID, name: String): Unit = {
    homeMap.remove(uuid, name)
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
    homeMap.put(homeOwner, homeName, newHome)
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

  private def save(): Unit = storage.saveMap(homeMap.toNormalMap)
}
