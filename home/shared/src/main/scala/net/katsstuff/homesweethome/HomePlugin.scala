package net.katsstuff.homesweethome

import java.nio.file.Path

import net.katsstuff.homesweethome.home.HomeHandler
import net.katsstuff.katlib.KatPlugin

trait HomePlugin extends KatPlugin {

  def configPath:  Path
  def storagePath: Path

  def homeHandler: HomeHandler
}
