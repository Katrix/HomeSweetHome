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
package net.katsstuff.homesweethome.command

import java.util.Locale

import cats.kernel.Monoid
import cats.syntax.all._
import cats.{FlatMap, ~>}
import net.katsstuff.homesweethome.home.{Home, HomeHandler}
import net.katsstuff.homesweethome.lib.LibPerm
import net.katsstuff.minejson.text._
import net.katsstuff.scammander.ScammanderHelper
import net.katstuff.katlib.algebras.{CommandSourceAccess, Localized, Pagination, PlayerAccess, Resource, Users}
import net.katstuff.katlib.command.KatLibCommands
import net.katstuff.katlib.syntax._
import shapeless.{Witness => W}

abstract class HomeCommandBundle[F[_]: FlatMap, G[_], Page: Monoid, CommandSource, Player, User, Location](FtoG: F ~> G)(
    implicit
    pagination: Pagination.Aux[F, CommandSource, Page],
    LocalizedF: Localized[F, CommandSource],
    homeHandler: HomeHandler[F, Player, User, Location],
    players: PlayerAccess[F, Player, User],
    users: Users[F, User, Player],
    commandSources: CommandSourceAccess[F, CommandSource],
    resource: Resource[F]
) extends KatLibCommands[F, G, Page, CommandSource, Player, User](FtoG) {

  val LocalizedG: Localized[G, CommandSource] = LocalizedF.mapK(FtoG)
  val LocalizedSF: Localized[SF, CommandSource] = LocalizedG.mapK(FtoSF)

  type OtherHomeArgs[A] = net.katsstuff.homesweethome.command.OtherHomeArgs[A, User]
  type OtherArgs[A]     = net.katsstuff.homesweethome.command.OtherArgs[A, User]

  def LocalizedDescription(key: String): CommandSource => Option[String] =
    KDescription(src => LocalizedF(src)(implicit locale => resource.get(key)))

  def localizedError[A](key: String): G[A] = FtoG(resource.get(key)).flatMap(e => Command.errorF(e))

  def button(button: Text, command: String): Text =
    (t"[$button]".onClick = ClickAction.RunCommand(command)).onHover = HoverAction.ShowText(t"$command")
  def manualButton(button: Text, command: String): Text =
    t"[[$button]]".onClick = ClickAction.SuggestCommand(command)

  final def homeNotFound(implicit locale: Locale): G[String] = ???

  implicit def homeParam: Parameter[Home] = new Parameter[Home] {
    override val name: String = "home"

    override def parse(source: CommandSource, extra: RunExtra): SF[Home] = LocalizedSF(source) { implicit locale =>
      for {
        arg     <- ScammanderHelper.firstArgAndDrop[G]
        player  <- UserValidator[Player].validate(source)
        uuid    <- FtoG(player.uniqueId)
        optHome <- FtoG(homeHandler.specificHome(uuid, arg.content))
        home    <- optHome.toFLift(homeNotFound)
      } yield home
    }

    override def suggestions(source: CommandSource, extra: TabExtra): SF[Seq[String]] =
      for {
        player   <- Command.liftFtoSF(UserValidator[Player].validate(source))
        uuid     <- Command.liftFtoSF(FtoG(player.uniqueId))
        allHomes <- Command.liftFtoSF(FtoG(homeHandler.allHomesForPlayer(uuid)))
        res      <- ScammanderHelper.suggestions(parse(source, tabExtraToRunExtra(extra)), allHomes.keys)
      } yield res
  }

  def specificUserHomeParam(user: User): Parameter[Home] = new Parameter[Home] {
    override val name: String = "home"

    override def parse(source: CommandSource, extra: RunExtra): SF[Home] = LocalizedSF(source) { implicit locale =>
      for {
        arg     <- ScammanderHelper.firstArgAndDrop[G]
        uuid    <- FtoG(user.uniqueId)
        optHome <- FtoG(homeHandler.specificHome(uuid, arg.content))
        home    <- optHome.toFLift(homeNotFound)
      } yield home
    }

    override def suggestions(source: CommandSource, extra: TabExtra): SF[Seq[String]] =
      for {
        uuid     <- FtoG(user.uniqueId)
        allHomes <- FtoG(homeHandler.allHomesForPlayer(uuid))
        res      <- ScammanderHelper.suggestions(parse(source, tabExtraToRunExtra(extra)), allHomes.keys)
      } yield res
  }

  implicit def homeWithNameParam: Parameter[HomeWithName] =
    new ProxyParameter[HomeWithName, Home] {
      override def param: Parameter[Home] = homeParam

      override def parse(source: CommandSource, extra: RunExtra): SF[HomeWithName] =
        for {
          xs   <- ScammanderHelper.getArgs[G]
          home <- param.parse(source, extra)
          ys   <- ScammanderHelper.getArgs[G]
        } yield {
          val argsTaken = xs.size - ys.size
          val name      = xs.take(argsTaken).map(_.content).mkString(" ")
          HomeWithName(name, home)
        }
    }

  def specificUserHomeWithNameParam(user: User): Parameter[HomeWithName] =
    new ProxyParameter[HomeWithName, Home] {
      override def param: Parameter[Home] = specificUserHomeParam(user)

      override def parse(source: CommandSource, extra: RunExtra): SF[HomeWithName] =
        for {
          xs   <- ScammanderHelper.getArgs[G]
          home <- param.parse(source, extra)
          ys   <- ScammanderHelper.getArgs[G]
        } yield {
          val argsTaken = xs.size - ys.size
          val name      = xs.take(argsTaken).map(_.content).mkString(" ")
          HomeWithName(name, home)
        }
    }

  implicit def otherHomeParam[A](implicit param: Parameter[A]): Parameter[OtherHomeArgs[A]] =
    new Parameter[OtherHomeArgs[A]] {
      private val flagParam = valueFlagParameter[W.`"other"`.T, OnlyOne[User]]

      private val emptyValueFlag = ValueFlag[W.`"other"`.T, OnlyOne[User]](None)

      //TODO: Add source to this and test for permissions
      override val name: String = s"home ${param.name} -other <target>"

      override def parse(source: CommandSource, extra: RunExtra): SF[OtherHomeArgs[A]] =
        for {
          hasOtherPerm <- Command.liftFtoSF(FtoG(source.hasPermission(LibPerm.HomeOther)))
          optOther     <- if (hasOtherPerm) flagParam.parse(source, extra) else SF.pure(emptyValueFlag)
          homeOwner    <- optOther.value.fold(UserValidator[User].validate(source))(user => F.pure(user.value))
          homeWithName <- specificUserHomeWithNameParam(homeOwner).parse(source, extra)
          extraParam   <- param.parse(source, extra)
        } yield OtherHomeArgs(optOther.value.isDefined, homeWithName.name, homeWithName.home, extraParam, homeOwner)

      override def suggestions(source: CommandSource, extra: TabExtra): SF[Seq[String]] =
        for {
          hasOtherPerm <- Command.liftFtoSF(FtoG(source.hasPermission(LibPerm.HomeOther)))
          base = ScammanderHelper.fallbackSuggestions(
            homeParam.suggestions(source, extra),
            param.suggestions(source, extra)
          )
          res <- if (hasOtherPerm) ScammanderHelper.fallbackSuggestions(flagParam.suggestions(source, extra), base)
          else base
        } yield res

      override def usage(source: CommandSource): G[String] =
        for {
          hasOtherPerm <- FtoG(source.hasPermission(LibPerm.HomeOther))
          paramUsage   <- param.usage(source)
          base = s"<home> $paramUsage"
          res <- if (hasOtherPerm) s"$base --other <player>" else base
        } yield res
    }

  implicit def otherParam[A](implicit param: Parameter[A]): Parameter[OtherArgs[A]] =
    new Parameter[OtherArgs[A]] {
      private val flagParam = valueFlagParameter[W.`"other"`.T, OnlyOne[User]]

      private val emptyValueFlag = ValueFlag[W.`"other"`.T, OnlyOne[User]](None)

      //TODO: Add source to this and test for permissions
      override val name: String = s"${param.name} -other <target>"

      override def parse(source: CommandSource, extra: RunExtra): SF[OtherArgs[A]] =
        for {
          hasOtherPerm <- Command.liftFtoSF(FtoG(source.hasPermission(LibPerm.HomeOther)))
          optOther     <- if (hasOtherPerm) flagParam.parse(source, extra) else SF.pure(emptyValueFlag)
          homeOwner    <- optOther.value.fold(UserValidator[User].validate(source))(user => F.pure(user.value))
          extraParam   <- param.parse(source, extra)
        } yield OtherArgs(optOther.value.isDefined, extraParam, homeOwner)

      override def suggestions(source: CommandSource, extra: TabExtra): SF[Seq[String]] =
        for {
          hasOtherPerm <- Command.liftFtoSF(FtoG(source.hasPermission(LibPerm.HomeOther)))
          base = param.suggestions(source, extra)
          res <- if (hasOtherPerm) ScammanderHelper.fallbackSuggestions(flagParam.suggestions(source, extra), base)
          else base
        } yield res

      override def usage(source: CommandSource): G[String] =
        for {
          hasOtherPerm <- FtoG(source.hasPermission(LibPerm.HomeOther))
          paramUsage   <- param.usage(source)
          res          <- if (hasOtherPerm) s"$paramUsage --other <player>" else paramUsage
        } yield res
    }

  def teleportError[A](implicit locale: Locale): G[A] =
    FtoG(resource.get("command.error.teleportError")).flatMap(Command.errorF(_))
}
