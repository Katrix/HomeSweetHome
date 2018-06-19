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

import java.util.{Locale, UUID}

import scala.concurrent.duration._
import scala.util.Try

import cats.effect.Async
import cats.effect.concurrent.MVar
import cats.syntax.all._
import cats.instances.option._
import net.katsstuff.homesweethome.lib.LibPerm
import net.katsstuff.homesweethome.{HomeConfig, HomeGlobal, NestedMap, Storage}
import net.katsstuff.minejson.text.Text
import net.katstuff.katlib.algebras.{Locations, PlayerAccess, Resource, Users}
import net.katstuff.katlib.syntax._

/**
  * The HomeHandler is what manages all the homes.
  */
trait HomeHandler[F[_], Player, User, Location] {

  /**
    * Clears the current homes and reloads them from disk.
    * @param storage The home storage where everything is saved
    */
  def reloadHomeData(storage: Map[UUID, Map[String, Home]]): F[Unit]

  /**
    * Add a home request
    */
  def addRequest(requester: Player, homeOwner: UUID, home: Home): F[Unit]

  /**
    * Removed a home request
    */
  def removeRequest(requester: Player, homeOwner: UUID): F[Unit]

  /**
    * Get a home request
    */
  def getRequest(requester: Player, homeOwner: UUID): F[Option[Home]]

  /**
    * Add a new invite to a specific home for a homeowner
    */
  def addInvite(target: Player, homeOwner: UUID, home: Home): F[Unit]

  /**
    * Removed an invite
    */
  def removeInvite(player: Player, homeOwner: UUID): F[Unit]

  /**
    * Check if a player is invited to a specific home
    */
  def isInvited(target: Player, homeOwner: UUID, home: Home): F[Boolean]

  /**
    * Gets all the homes for a specific player.
    *
    * @return A map containing all the homes for a specific player.
    * The map itself is a copy.
    */
  def allHomesForPlayer(uuid: UUID): F[Map[String, Home]]

  /**
    * Gets a specific home for a player.
    *
    * @param uuid UUID of player
    * @param name Name of player
    * @return Home if it was found
    */
  def specificHome(uuid: UUID, name: String): F[Option[Home]]

  /**
    * Check if a player has a home with the specific name.
    *
    * @param uuid UUID of player
    * @param name Name of home
    * @return If home exist
    */
  def homeExist(uuid: UUID, name: String): F[Boolean]

  /**
    * Makes a new home for a player with a specific location.
    *
    * @param uuid The UUID of the player
    * @param name The name of the new home
    * @param location The location of the new home
    */
  def makeHome(uuid: UUID, name: String, location: Location): F[Unit]

  /**
    * Deletes a home with the specific name.
    *
    * @param uuid The UUID if the player to delete the home for.
    * @param name The name of the home to delete.
    */
  def deleteHome(uuid: UUID, name: String): F[Unit]

  /**
    * Updates an existing home
    *
    * @param homeOwner The owner of the home
    * @param homeName The name of the home to modify
    * @param newHome The new home to use
    */
  def updateHome(homeOwner: UUID, homeName: String, newHome: Home): F[Unit]

  def canCreateHome(player: Player, name: String): F[Either[Text, Unit]]

  def canCreateHome(name: String, subject: User, location: Location)(implicit locale: Locale): F[Either[Text, Unit]]

  /**
    * Check if a player has reached his home limit.
    *
    * @param user The player
    */
  def hasReachedHomeLimit(user: User): F[Boolean]

  /**
    * Check if a home can be placed at a specific location
    * @param user The user to check for
    * @param location The location to check
    * @return Left with error message if it's an invalid location, else right
    */
  def canMakeHomeAtLocation(user: User, location: Location)(implicit locale: Locale): F[Either[Text, Unit]]

  /**
    * The amount of homes a player can have
    */
  def getHomeLimit(user: User): F[Int]

  /**
    * The amount of residents a home for a player can have
    */
  def getResidentLimit(user: User): F[Int]
}

class IOHomeHandler[F[_], Player, User, Location](config: MVar[F, HomeConfig])(
    implicit nestedMap: NestedMap[F],
    locations: Locations[F, Location, Player],
    resource: Resource[F],
    storage: Storage[F],
    players: PlayerAccess[F, Player, User],
    users: Users[F, User, Player],
    global: HomeGlobal[F],
    F: Async[F]
) extends HomeHandler[F, Player, User, Location] {

  private val homeMap: nestedMap.Map[UUID, String, Home] = nestedMap.standardEmpty[UUID, String, Home]

  private val requests: nestedMap.Map[Player, UUID, Home] =
    nestedMap.weakCacheExpireAfterWrite(config.read.map(_.timeout.seconds))
  private val invites: nestedMap.Map[Player, UUID, Home] =
    nestedMap.weakCacheExpireAfterWrite(config.read.map(_.timeout.seconds))

  override def reloadHomeData(storage: Map[UUID, Map[String, Home]]): F[Unit] =
    for {
      _ <- nestedMap.clear(homeMap)
      _ <- nestedMap.clear(requests)
      _ <- nestedMap.clear(invites)
      _ <- nestedMap.addAll(homeMap)(storage)
    } yield ()

  override def addRequest(requester: Player, homeOwner: UUID, home: Home): F[Unit] =
    nestedMap.put(requests)(requester, homeOwner, home)

  override def removeRequest(requester: Player, homeOwner: UUID): F[Unit] =
    nestedMap.remove(requests)(requester, homeOwner)

  override def getRequest(requester: Player, homeOwner: UUID): F[Option[Home]] =
    nestedMap.get(requests)(requester, homeOwner)

  override def addInvite(target: Player, homeOwner: UUID, home: Home): F[Unit] =
    nestedMap.put(invites)(target, homeOwner, home)

  override def removeInvite(player: Player, homeOwner: UUID): F[Unit] =
    nestedMap.remove(invites)(player, homeOwner)

  override def isInvited(target: Player, homeOwner: UUID, home: Home): F[Boolean] =
    nestedMap.containsValue(invites)(target, homeOwner, home)

  override def allHomesForPlayer(uuid: UUID): F[Map[String, Home]] = nestedMap.getAll(homeMap)(uuid)

  override def specificHome(uuid: UUID, name: String): F[Option[Home]] = nestedMap.get(homeMap)(uuid, name)

  override def homeExist(uuid: UUID, name: String): F[Boolean] = nestedMap.containsKey(homeMap)(uuid, name)

  override def makeHome(uuid: UUID, name: String, location: Location): F[Unit] =
    for {
      (x, y, z, yaw, pitch, worldId) <- (
        location.x,
        location.y,
        location.z,
        location.yaw,
        location.pitch,
        location.worldId
      ).tupled
      _ <- nestedMap.put(homeMap)(uuid, name, Home(x, y, z, yaw, pitch, worldId, Nil))
      _ <- save
    } yield ()

  override def deleteHome(uuid: UUID, name: String): F[Unit] = nestedMap.remove(homeMap)(uuid, name) *> save

  override def updateHome(homeOwner: UUID, homeName: String, newHome: Home): F[Unit] =
    nestedMap.put(homeMap)(homeOwner, homeName, newHome) *> save

  override def canCreateHome(player: Player, name: String): F[Either[Text, Unit]] =
    for {
      location <- player.getLocation
      res      <- canCreateHome(name, players.userForPlayer(player), location)
    } yield res

  override def canCreateHome(name: String, user: User, location: Location)(
      implicit locale: Locale
  ): F[Either[Text, Unit]] =
    for {
      uuid         <- user.uniqueId
      exists       <- homeExist(uuid, name)
      reachedLimit <- hasReachedHomeLimit(user)
      limitText    <- resource.getText("command.error.homeLimitReached")
      res1 = Either.cond(exists || !reachedLimit, (), limitText)
      res2 <- canMakeHomeAtLocation(user, location)
    } yield res1.flatMap(_ => res2)

  override def hasReachedHomeLimit(user: User): F[Boolean] =
    for {
      uuid     <- user.uniqueId
      allHomes <- allHomesForPlayer(uuid)
      limit    <- getHomeLimit(user)
    } yield allHomes.size < limit

  override def canMakeHomeAtLocation(user: User, location: Location)(implicit locale: Locale): F[Either[Text, Unit]] =
    for {
      isSafe                <- location.makeSafe.map(_.isDefined)
      optWorldName          <- location.worldName
      optHasPermissionWorld <- optWorldName.traverse(worldName => user.hasPermission(LibPerm.HomeSetWorld(worldName)))
      notSafeText           <- resource.getText("command.error.worldNotFound")
      worldNotFoundText     <- resource.getText("command.error.worldNotFound")
      notAllowedWorldText   <- resource.getText("command.error.notAllowedWorld")
    } yield {
      for {
        hasWorldPermission <- optHasPermissionWorld.toRight(worldNotFoundText)
        _                  <- Either.cond(isSafe, (), notSafeText)
        _                  <- Either.cond(hasWorldPermission, (), notAllowedWorldText)
      } yield ()
    }

  override def getHomeLimit(user: User): F[Int] =
    user.getOption(LibPerm.HomeLimitOption).flatMap { optStr =>
      optStr.flatMap(s => Try(s.toInt).map(F.pure).toOption).getOrElse(config.read.map(_.homeLimitDefault))
    }

  override def getResidentLimit(user: User): F[Int] =
    user.getOption(LibPerm.ResidentLimitOption).flatMap { optStr =>
      optStr.flatMap(s => Try(s.toInt).map(F.pure).toOption).getOrElse(config.read.map(_.residentLimitDefault))
    }

  private def save: F[Unit] =
    for {
      normalMap <- nestedMap.toNormalMap(homeMap)
      _         <- storage.save(global.storagePath, normalMap)
    } yield ()
}
