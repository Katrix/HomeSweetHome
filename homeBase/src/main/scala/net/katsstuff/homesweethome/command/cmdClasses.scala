package net.katsstuff.homesweethome.command

import java.util.Locale

import cats.Monad
import cats.syntax.all._
import net.katsstuff.homesweethome.home.Home
import net.katsstuff.minejson.text.Text
import net.katstuff.katlib.algebras.Resource

case class HomeWithName(name: String, home: Home)

case class OtherHomeArgs[A, User](isOther: Boolean, rawHomeName: String, home: Home, args: A, homeOwner: User) {
  def homeName[F[_]](implicit users: Users[F, User], F: Monad[F]): F[String] =
    if (isOther) users.name(homeOwner).map(name => s""""$rawHomeName" for $name""") else F.pure(s""""$rawHomeName"""")
  def messageWithHomeName[F[_]](key: String, args: (String, String)*)(
      implicit locale: Locale,
      resource: Resource[F]
  ): F[Text] = ???
}

case class OtherArgs[A, User](isOther: Boolean, args: A, homeOwner: User)
