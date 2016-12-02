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

import java.util.{UUID, List => JList}

import scala.collection.JavaConverters._

import com.google.common.reflect.TypeToken

import io.github.katrix.homesweethome.home.V1Home
import io.github.katrix.katlib.helper.Implicits.{RichConfigurationNode, typeToken}
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.objectmapping.ObjectMappingException
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer

object HomeSerializer extends TypeSerializer[V1Home] {

	private final val X         = "x"
	private final val Y         = "y"
	private final val Z         = "z"
	private final val Yaw       = "yaw"
	private final val Pitch     = "pitch"
	private final val World     = "world"
	private final val Residents = "residents"

	override def serialize(`type`: TypeToken[_], home: V1Home, value: ConfigurationNode): Unit = {
		val uuidListType = typeToken[JList[UUID]]
		value.getNode(X).value_=(home.x)
		value.getNode(Y).value_=(home.y)
		value.getNode(Z).value_=(home.z)
		value.getNode(Yaw).value_=(home.yaw)
		value.getNode(Pitch).value_=(home.pitch)
		value.getNode(World).value_=(home.worldUuid)
		value.getNode(Residents).value_=(home.residents.asJava)(uuidListType)
	}

	override def deserialize(`type`: TypeToken[_], value: ConfigurationNode): V1Home = {
		(for {
			x <- Option(value.getNode(X).value[Double])
			y <- Option(value.getNode(Y).value[Double])
			z <- Option(value.getNode(Z).value[Double])
			yaw <- Option(value.getNode(Yaw).value[Float])
			pitch <- Option(value.getNode(Pitch).value[Float])
			worldUUID <- Option(value.getNode(World).value[UUID])
		} yield {
			val residents = Option(value.getNode(Residents).list[UUID]).getOrElse(Seq())
			V1Home(x, y, z, yaw, pitch, worldUUID, residents)
		}).getOrElse(throw new ObjectMappingException("Missing values"))
	}
}
