package net.katsstuff.homesweethome.command

import java.util.Locale

import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.text.Text

import net.katsstuff.homesweethome.home.Home

case class HomeWithName(name: String, home: Home)

case class OtherHomeArgs[A](isOther: Boolean, rawHomeName: String, home: Home, args: A, homeOwner: User) {
  def homeName: String = if (isOther) s""""$rawHomeName" for ${homeOwner.getName}""" else s""""$rawHomeName""""
  def messageWithHomeName(key: String, args: (String, String)*)(implicit locale: Locale): Text = ???
}

case class OtherArgs[A](isOther: Boolean, args: A, homeOwner: User)