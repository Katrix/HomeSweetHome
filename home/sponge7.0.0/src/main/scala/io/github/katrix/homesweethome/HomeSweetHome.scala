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
package io.github.katrix.homesweethome

import java.nio.file.Path
import java.util.UUID

import scala.util.Try

import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.{GameConstructionEvent, GameInitializationEvent}
import org.spongepowered.api.plugin.{Dependency, Plugin, PluginContainer}
import org.spongepowered.api.service.permission.Subject

import com.google.inject.Inject

import io.github.katrix.homesweethome.command.CmdHome
import io.github.katrix.homesweethome.home.{Home, HomeHandler, HomeV1}
import io.github.katrix.homesweethome.lib.{LibPerm, LibPlugin}
import io.github.katrix.homesweethome.persistant.{HomeConfig, HomeConfigLoader, HomeSerializer, StorageLoader}
import io.github.katrix.katlib.helper.Implicits.{RichOptional, typeToken}
import io.github.katrix.katlib.lib.LibKatLibPlugin
import io.github.katrix.katlib.serializer.TypeSerializerImpl
import io.github.katrix.katlib.serializer.TypeSerializerImpl.typeSerializer
import io.github.katrix.katlib.{ImplKatPlugin, KatLib, KatPlugin700}
import ninja.leaping.configurate.objectmapping.serialize.{TypeSerializer, TypeSerializers}

object HomeSweetHome {

  final val Version         = s"2.3.0-${KatLib.CompiledAgainst}"
  final val ConstantVersion = "2.3.0-7.0.0"
  assert(Version == ConstantVersion)

  private var _plugin: HomeSweetHome = _
  implicit def plugin: HomeSweetHome = _plugin

  def init(event: GameInitializationEvent): Unit = {
    val serializers             = TypeSerializers.getDefaultSerializers
    implicit val uuidSerializer = TypeSerializerImpl.fromTypeSerializer(serializers.get(typeToken[UUID]), classOf[UUID])
    serializers.registerType(typeToken[HomeV1], HomeSerializer)
    serializers.registerType(typeToken[Home], implicitly[TypeSerializer[Home]])
    plugin._config = plugin.configLoader.loadData

    val homeHandler = new HomeHandler(plugin.storageLoader, plugin.config) {
      override def getHomeLimit(player: Subject): Int =
        player
          .getOption(LibPerm.HomeLimitOption)
          .toOption
          .flatMap(s => Try(s.toInt).toOption)
          .getOrElse(plugin.config.homeLimitDefault.value)
      override def getResidentLimit(player: Subject): Int =
        player
          .getOption(LibPerm.ResidentLimitOption)
          .toOption
          .flatMap(s => Try(s.toInt).toOption)
          .getOrElse(plugin.config.residentLimitDefault.value)
    }
    homeHandler.reloadHomeData()

    val cmdHome = new CmdHome(homeHandler)
    cmdHome.registerHelp()
    Sponge.getCommandManager.register(plugin, plugin.pluginCmd.commandSpec, plugin.pluginCmd.aliases: _*)
    Sponge.getCommandManager.register(plugin, cmdHome.commandSpec, cmdHome.aliases: _*)
    Sponge.getCommandManager.register(plugin, cmdHome.homeList.commandSpec, "homes")
  }
}

@Plugin(
  id = LibPlugin.Id,
  name = LibPlugin.Name,
  version = HomeSweetHome.ConstantVersion,
  dependencies = Array(new Dependency(id = LibKatLibPlugin.Id, version = KatLib.ConstantVersion))
)
class HomeSweetHome @Inject()(
    logger: Logger,
    @ConfigDir(sharedRoot = false) cfgDir: Path,
    spongeContainer: PluginContainer
) extends ImplKatPlugin(logger, cfgDir, spongeContainer)
    with KatPlugin700 {

  implicit val plugin: HomeSweetHome = this

  private lazy val configLoader = new HomeConfigLoader(configDir)
  lazy val storageLoader        = new StorageLoader(configDir)

  private var _config: HomeConfig = _
  def config:          HomeConfig = _config

  @Listener
  def gameConstruct(event: GameConstructionEvent) {
    HomeSweetHome._plugin = this
  }

  @Listener
  def init(event: GameInitializationEvent): Unit =
    HomeSweetHome.init(event)
}
