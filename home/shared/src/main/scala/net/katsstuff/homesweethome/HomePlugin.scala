package net.katsstuff.homesweethome

import java.nio.file.Path

import io.github.katrix.katlib.KatPlugin
import net.katsstuff.homesweethome.home.HomeHandler

trait HomePlugin extends KatPlugin {

  def configPath:  Path
  def storagePath: Path

  def homeHandler: HomeHandler
}
