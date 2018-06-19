package net.katsstuff.homesweethome

import java.nio.file.Path

import net.katstuff.katlib.algebras.PluginGlobal

trait HomeGlobal[F[_]] extends PluginGlobal[F] {

  def storagePath: Path
  def configPath: Path

}
