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
package io.github.katrix.homesweethome.command

import java.util
import java.util.Collections
import java.util.regex.Pattern

import javax.annotation.Nullable

import scala.collection.JavaConverters._

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.{ArgumentParseException, CommandArgs, CommandContext, CommandElement}
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.util.SpongeApiTranslationHelper.t

import io.github.katrix.homesweethome.home.HomeHandler
import io.github.katrix.katlib.helper.Implicits._

//Copy of PatternMatchingCommandElement to get player and use more than one string
class CommandElementHome(@Nullable val key: Text, homeHandler: HomeHandler) extends CommandElement(key) {

  private final val NullKeyArg = t("argument")

  @Nullable
  @throws[ArgumentParseException]
  protected def parseValue(source: CommandSource, args: CommandArgs): AnyRef =
    source match {
      case player: Player =>
        args.next
        val unformattedPattern = args.getRaw.substring(args.getRawPosition)
        while (args.hasNext) args.next //Consume remaining args

        val pattern         = getFormattedPattern(unformattedPattern)
        val filteredChoices = getChoices(source).filter(element => pattern.matcher(element).find())
        for (el <- filteredChoices) {
          // Match a single value
          if (el.equalsIgnoreCase(unformattedPattern)) return Collections.singleton(getValue(player, el))
        }

        val ret = filteredChoices.map(c => getValue(player, c))
        if (!ret.iterator.hasNext) throw args.createError(t("No values matching pattern '%s' present for %s!", unformattedPattern, {
          if (getKey == null) NullKeyArg else getKey
        }))

        ret.asJava
      case _ => throw args.createError(t"This command can only be used by players")
    }

  def complete(src: CommandSource, args: CommandArgs, context: CommandContext): util.List[String] = {
    val choices = getChoices(src)
    args.nextIfPresent.toOption match {
      case Some(nextArg) => choices.filter(input => getFormattedPattern(nextArg).matcher(input).find()).toSeq.asJava
      case None          => choices.toSeq.asJava
    }
  }

  private def getFormattedPattern(input: String): Pattern = {
    val usedInput = if (!input.startsWith("^")) {
      // Anchor matches to the beginning -- this lets us use find()
      "^" + input
    } else input
    Pattern.compile(usedInput, Pattern.CASE_INSENSITIVE)
  }

  protected def getChoices(source: CommandSource): Iterable[String] = source match {
    case player: Player => homeHandler.allHomesForPlayer(player.getUniqueId).keys
    case _ => Nil
  }

  @throws[IllegalArgumentException]
  protected def getValue(player: Player, choice: String): AnyRef =
    homeHandler.specificHome(player.getUniqueId, choice).map((_, choice)).getOrElse(throw new IllegalArgumentException)
}
