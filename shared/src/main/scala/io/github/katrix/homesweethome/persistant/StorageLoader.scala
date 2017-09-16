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
package io.github.katrix.homesweethome.persistant

import java.nio.file.Path
import java.util.{Map => JMap}
import java.util.UUID

import scala.collection.JavaConverters._

import io.github.katrix.homesweethome.home.{Home, HomeV1}
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits.typeToken
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.ConfigurateBase
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.gson.GsonConfigurationLoader

class StorageLoader(dir: Path)(implicit plugin: KatPlugin)
    extends ConfigurateBase[Map[UUID, Map[String, Home]], ConfigurationNode, GsonConfigurationLoader](
      dir,
      "storage.json",
      path => GsonConfigurationLoader.builder().setPath(path).build()
    ) {

  private val mapTypeToken   = typeToken[JMap[UUID, JMap[String, Home]]]
  private val v1MapTypeToken = typeToken[JMap[UUID, JMap[String, HomeV1]]]

  override def loadData: Map[UUID, Map[String, Home]] = {
    versionNode.getString("2") match {
      case "1" => loadV1Homes()
      case "2" => loadV2Homes()
    }
  }

  private def loadV1Homes(): Map[UUID, Map[String, Home]] = {
    val ver1: Map[UUID, Map[String, Home]] = Option(homeNode.getValue(v1MapTypeToken)) match {
      case Some(map) =>
        scalaMap(map).map { case (k, v) => k -> scalaMap(v).map { case (k1, v1) => k1 -> v1.toCurrent } }
      case None =>
        LogHelper.error("Could not load homes from storage.")
        Map.empty
    }
    //We save here to go up to v2
    saveData(ver1)
    ver1
  }

  private def loadV2Homes(): Map[UUID, Map[String, Home]] = {
    Option(homeNode.getValue(mapTypeToken)) match {
      case Some(map) =>
        scalaMap(map).map { case (k, v) => k -> scalaMap(v) }
      case None =>
        if(versionNode.getString != null) {
          LogHelper.error("Could not load homes from storage.")
        }
        else {
          saveData(Map.empty)
        }

        Map.empty
    }
  }

  private def scalaMap[A, B](map: JMap[A, B]): Map[A, B] = Map(map.asScala.toSeq: _*)

  override def saveData(data: Map[UUID, Map[String, Home]]): Unit = {
    versionNode.setValue("2")

    val javaMap = data.map { case (key, value) => (key, value.asJava) }.asJava
    homeNode.setValue(mapTypeToken, javaMap)

    saveFile()
  }

  def saveMap(data: Map[UUID, Map[String, Home]]): Unit = saveData(data)

  private def homeNode: ConfigurationNode = cfgRoot.getNode("home")

  private def versionNode: ConfigurationNode = cfgRoot.getNode("version")
}
