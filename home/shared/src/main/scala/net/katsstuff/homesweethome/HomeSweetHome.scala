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

import scala.concurrent.ExecutionContext.Implicits.global

import org.slf4j.Logger
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.plugin.{Plugin, PluginContainer}

import com.google.inject.Inject

import cats.effect.IO
import cats.syntax.all._
import io.github.katrix.katlib.ImplKatPlugin
import io.github.katrix.katlib.helper.LogHelper
import net.katsstuff.homesweethome.home.HomeHandler
import net.katsstuff.homesweethome.lib.LibPlugin

object HomeSweetHome {

  def init(event: GameInitializationEvent)(implicit plugin: HomeSweetHome): Unit = {
    val loadConfig  = IO.shift *> HomeConfig.load(plugin.configPath)
    val loadStorage = IO.shift *> Storage.load(plugin.storagePath)

    val startup = (loadConfig, loadStorage).parMapN {
      case (config, storage) =>
        for {
          _ <- IO(plugin.homeHandler.reloadHomeData(storage, config))
          _ <- IO(command.HomeCmds.HomeCmd.register(this, Seq("home")))
          _ <- IO(command.HomeCmds.HomeListCmd.register(this, Seq("homes")))
          _ <- IO(command.BaseCmds.HomeSweetHomeCmd.register(this, Seq("homesweethome", "hsh")))
        } yield ()
    }.flatten

    startup.unsafeRunAsync(_.left.foreach(e => LogHelper.error("Couldn't load config or storage", e)))
  }
}

@Plugin(id = LibPlugin.Id, name = LibPlugin.Name, version = HSHConstants.ConstantVersion)
class HomeSweetHome @Inject()(
    logger: Logger,
    @ConfigDir(sharedRoot = false) cfgDir: Path,
    spongeContainer: PluginContainer
) extends ImplKatPlugin(logger, cfgDir, spongeContainer)
    with HomePlugin {

  implicit val plugin: HomeSweetHome = this

  lazy val storagePath: Path = cfgDir.resolve("storage.json")
  lazy val configPath:  Path = cfgDir.resolve("config.conf")

  val homeHandler = new HomeHandler

  @Listener
  def init(event: GameInitializationEvent): Unit = HomeSweetHome.init(event)
}
