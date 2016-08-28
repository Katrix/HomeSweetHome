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
import java.util
import java.util.UUID

import scala.collection.JavaConverters._

import com.google.common.reflect.{TypeParameter, TypeToken}

import io.github.katrix.homesweethome.home.Home
import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits.typeToken
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.ConfigurateBase
import ninja.leaping.configurate.commented.CommentedConfigurationNode

class StorageLoader(dir: Path)(implicit plugin: KatPlugin) extends ConfigurateBase[Map[UUID, Map[String, Home]]](dir, "storage", true) {

	private val mapTypeToken = mapOf(typeToken[UUID], mapOf[String, Home])

	private def mapOf[K: TypeToken, V: TypeToken]: TypeToken[util.Map[K, V]] = {
		new TypeToken[util.Map[K, V]]() {}
			.where(new TypeParameter[K] {}, implicitly[TypeToken[K]])
			.where(new TypeParameter[V] {}, implicitly[TypeToken[V]])
	}

	override protected def loadVersionedData(version: String): Map[UUID, Map[String, Home]] = version match {
		case "1" =>
			val node = homeNode
			Option(node.getValue(mapTypeToken)) match {
				case Some(map) =>
					Map(map.asScala.map { case (key, value) => (key, Map(value.asScala.toSeq: _*)) }.toSeq: _*)
				case None =>
					LogHelper.error("Could not load homes from storage.")
					default
			}
		case _ =>
			LogHelper.error("Invalid version for homes in storage.")
			default
	}

	override protected val default: Map[UUID, Map[String, Home]] = Map()

	override protected def saveData(data: Map[UUID, Map[String, Home]]): Unit = {
		versionNode.setValue("1")

		val javaMap = data.map { case (key, value) => (key, value.asJava) }.asJava
		homeNode.setValue(mapTypeToken, javaMap)

		saveFile()
	}

	def saveMap(data: Map[UUID, Map[String, Home]]): Unit = saveData(data)

	private def homeNode: CommentedConfigurationNode = cfgRoot.getNode("home")
}
