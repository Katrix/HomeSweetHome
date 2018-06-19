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

import java.nio.file.Path
import java.util.UUID

import cats.MonadError
import cats.syntax.all._
import io.circe._
import io.circe.syntax._
import net.katsstuff.homesweethome.home.Home
import net.katstuff.katlib.algebras.{FileAccess, LogHelper}

trait Storage[F[_]] {

  def load(path: Path): F[Map[UUID, Map[String, Home]]]

  def save(path: Path, homes: Map[UUID, Map[String, Home]]): F[Unit]
}

class DefaultStorage[F[_]](
    implicit log: LogHelper[F],
    files: FileAccess[F],
    F: MonadError[F, Throwable]
) {

  def load(path: Path): F[Map[UUID, Map[String, Home]]] =
    for {
      _          <- log.info("Loading homes")
      fileExists <- files.exists(path)
      res <- if (fileExists) {
        for {
          content <- files.readFile(path)
          json    <- F.fromEither(parser.parse(content))
          cursor = json.hcursor
          version <- F.fromEither(cursor.get[Int]("version"))
          data <- version match {
            case 2 => F.fromEither(cursor.get[Map[UUID, Map[String, Home]]]("home"))
            case _ => F.raiseError[Map[UUID, Map[String, Home]]](new Exception("Unsupported storage version"))
          }
        } yield data
      } else {
        log.info("No homes found").as(Map.empty[UUID, Map[String, Home]])
      }
    } yield res

  def save(path: Path, homes: Map[UUID, Map[String, Home]]): F[Unit] = {
    val printer = Printer.noSpaces.copy(dropNullValues = true)
    val json    = Json.obj("version" -> 2.asJson, "home" -> homes.asJson)

    files.saveFile(path, json.pretty(printer))
  }
}
