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

import java.util.Locale

import org.jetbrains.annotations.PropertyKey
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.{Player, User}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.world.{Location, World}

import net.katsstuff.katlib.KatPlugin
import net.katsstuff.katlib.command.KatLibCommands
import net.katsstuff.katlib.helper.Implicits._
import net.katsstuff.katlib.i18n.Localized
import net.katsstuff.homesweethome.home.{Home, HomeHandler}
import net.katsstuff.homesweethome.lib.LibPerm
import net.katsstuff.scammander.ScammanderHelper
import shapeless.{Witness => W}

package object command extends KatLibCommands {

  def LocalizedDescription(
      @PropertyKey(resourceBundle = HSHResource.ResourceLocation) key: String
  ): CommandSource => Option[Text] =
    Description(src => Localized(src)(implicit locale => HSHResource.getText(key)))

  def button(button: Text, command: String): Text =
    t"[$button]".toBuilder.onClick(TextActions.runCommand(command)).onHover(TextActions.showText(t"$command")).build()
  def manualButton(button: Text, command: String): Text =
    t"[[$button]]".toBuilder.onClick(TextActions.suggestCommand(command)).build()

  override implicit def plugin: KatPlugin = ??? //TODO: Remove

  def homeHandler(implicit plugin: HomePlugin): HomeHandler = plugin.homeHandler

  def confirmButton(button: Text, text: String): Text = manualButton(button, text)

  final val HomeNotFound = "No home with that name found"

  implicit def homeParam(implicit plugin: HomePlugin): Parameter[Home] = new Parameter[Home] {
    override def name: String = "home"

    override def parse(
        source: CommandSource,
        extra: Unit,
        xs: List[RawCmdArg]
    ): CommandStep[(List[RawCmdArg], Home)] = {
      stringParam.parse(source, extra, xs).flatMap {
        case (ys, arg) =>
          UserValidator[Player]
            .validate(source)
            .flatMap { player =>
              homeHandler
                .specificHome(player.getUniqueId, arg)
                .toStep(HomeNotFound)
            }
            .map(ys -> _)
      }
    }

    override def suggestions(
        source: CommandSource,
        extra: Location[World],
        xs: List[RawCmdArg]
    ): Either[List[RawCmdArg], Seq[String]] =
      UserValidator[Player]
        .validate(source)
        .left
        .map(_ => xs)
        .flatMap { player =>
          ScammanderHelper
            .suggestions(parse(source, extra, _), xs, homeHandler.allHomesForPlayer(player.getUniqueId).keys)
        }
  }

  def specificUserHomeParam(user: User)(implicit plugin: HomePlugin): Parameter[Home] = new Parameter[Home] {
    override def name: String = "home"

    override def parse(
        source: CommandSource,
        extra: Unit,
        xs: List[RawCmdArg]
    ): CommandStep[(List[RawCmdArg], Home)] = {
      stringParam.parse(source, extra, xs).flatMap {
        case (ys, arg) =>
          homeHandler
            .specificHome(user.getUniqueId, arg)
            .toStep(HomeNotFound)
            .map(ys -> _)
      }
    }

    override def suggestions(
        source: CommandSource,
        extra: Location[World],
        xs: List[RawCmdArg]
    ): Either[List[RawCmdArg], Seq[String]] =
      ScammanderHelper.suggestions(parse(source, extra, _), xs, homeHandler.allHomesForPlayer(user.getUniqueId).keys)
  }

  implicit def homeWithNameParam(implicit plugin: HomePlugin): Parameter[HomeWithName] =
    new ProxyParameter[HomeWithName, Home] {
      override def param: Parameter[Home] = homeParam

      override def parse(
          source: CommandSource,
          extra: Unit,
          xs: List[RawCmdArg]
      ): CommandStep[(List[RawCmdArg], HomeWithName)] = {
        param.parse(source, extra, xs).map {
          case (ys, home) =>
            val argsTaken = xs.size - ys.size
            val name      = xs.take(argsTaken).map(_.content).mkString(" ")
            (ys, HomeWithName(name, home))
        }
      }
    }

  def specificUserHomeWithNameParam(user: User)(implicit plugin: HomePlugin): Parameter[HomeWithName] =
    new ProxyParameter[HomeWithName, Home] {
      override def param: Parameter[Home] = specificUserHomeParam(user)

      override def parse(
          source: CommandSource,
          extra: Unit,
          xs: List[RawCmdArg]
      ): CommandStep[(List[RawCmdArg], HomeWithName)] = {
        param.parse(source, extra, xs).map {
          case (ys, home) =>
            val argsTaken = xs.size - ys.size
            val name      = xs.take(argsTaken).map(_.content).mkString(" ")
            (ys, HomeWithName(name, home))
        }
      }
    }

  implicit def otherHomeParam[A](implicit plugin: HomePlugin, param: Parameter[A]): Parameter[OtherHomeArgs[A]] =
    new Parameter[OtherHomeArgs[A]] {
      private val flagParam     = valueFlagParameter[W.`"other"`.T, OnlyOne[User]]
      private val userValidator = UserValidator[User]

      //TODO: Add source to this and test for permissions
      override def name: String = s"home ${param.name} -other <target>"

      override def parse(
          source: CommandSource,
          extra: Unit,
          xs: List[RawCmdArg]
      ): CommandStep[(List[RawCmdArg], OtherHomeArgs[A])] = {
        for {
          t1 <- if (source.hasPermission(LibPerm.HomeOther)) flagParam.parse(source, extra, xs)
          else Right(xs -> ValueFlag(None))
          homeOwner <- t1._2.value.fold(userValidator.validate(source))(player => Right(player.value))
          t2        <- specificUserHomeWithNameParam(homeOwner).parse(source, extra, t1._1)
          t3        <- param.parse(source, extra, t2._1)
        } yield t3._1 -> OtherHomeArgs(t1._2.value.isDefined, t2._2.name, t2._2.home, t3._2, homeOwner)
      }

      override def suggestions(
          source: CommandSource,
          extra: Location[World],
          xs: List[RawCmdArg]
      ): Either[List[RawCmdArg], Seq[String]] = {
        val flagSuggestions =
          if (source.hasPermission(LibPerm.HomeOther)) flagParam.suggestions(source, extra, xs) else Left(xs)
        println(flagSuggestions)
        flagSuggestions match {
          case Left(ys) =>
            for {
              zs <- homeParam.suggestions(source, extra, ys).left
              ws <- param.suggestions(source, extra, zs).left
            } yield ws
          case Right(suggestions) => Right(suggestions ++ homeParam.suggestions(source, extra, xs).getOrElse(Nil))
        }
      }

      override def usage(source: CommandSource): String = {
        val base = s"<home> ${param.usage(source)}"
        if (source.hasPermission(LibPerm.HomeOther)) s"$base --other <player>" else base
      }
    }

  implicit def otherParam[A](implicit param: Parameter[A]): Parameter[OtherArgs[A]] = new Parameter[OtherArgs[A]] {
    private val flagParam       = valueFlagParameter[W.`"other"`.T, OnlyOne[User]]
    private val playerValidator = UserValidator[User]

    //TODO: Add source to this and test for permissions
    override def name: String = s"${param.name} -other <target>"

    override def parse(
        source: CommandSource,
        extra: Unit,
        xs: List[RawCmdArg]
    ): CommandStep[(List[RawCmdArg], OtherArgs[A])] = {
      for {
        t1 <- if (source.hasPermission(LibPerm.HomeOther)) flagParam.parse(source, extra, xs)
        else Right(xs -> ValueFlag(None))
        homeOwner <- t1._2.value.fold(playerValidator.validate(source))(player => Right(player.value))
        t2        <- param.parse(source, extra, t1._1)
      } yield t2._1 -> OtherArgs(t1._2.value.isDefined, t2._2, homeOwner)
    }

    override def suggestions(
        source: CommandSource,
        extra: Location[World],
        xs: List[RawCmdArg]
    ): Either[List[RawCmdArg], Seq[String]] = {
      val flagSuggestions =
        if (source.hasPermission(LibPerm.HomeOther)) flagParam.suggestions(source, extra, xs) else Left(xs)
      flagSuggestions match {
        case Left(ys) =>
          for {
            zs <- param.suggestions(source, extra, ys).left
          } yield zs
        case Right(suggestions) => Right(suggestions ++ param.suggestions(source, extra, xs).getOrElse(Nil))
      }
    }

    override def usage(source: CommandSource): String = {
      val base = param.usage(source)
      if (source.hasPermission(LibPerm.HomeOther)) s"$base --other <player>" else base
    }
  }

  def teleportError(implicit locale: Locale): CommandFailure =
    Command.error(HSHResource.get("command.error.teleportError"))
}
