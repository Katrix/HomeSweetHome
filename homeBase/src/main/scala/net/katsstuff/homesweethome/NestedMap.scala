package net.katsstuff.homesweethome

import scala.concurrent.duration.Duration

trait NestedMap[F[_]] {
  type Map[K1, K2, V]

  def standardEmpty[K1, K2, V]: Map[K1, K2, V]

  def weakCacheExpireAfterWrite[K1, K2, V](duration: F[Duration]): Map[K1, K2, V]

  def clear[K1, K2, V](map: Map[K1, K2, V]): F[Unit]

  def addAll[K1, K2, V](map: Map[K1, K2, V])(normalMap: Predef.Map[K1, Predef.Map[K2, V]]): F[Unit]

  def get[K1, K2, V](map: Map[K1, K2, V])(k1: K1, k2: K2): F[Option[V]]

  def put[K1, K2, V](map: Map[K1, K2, V])(k1: K1, k2: K2, v: V): F[Unit]

  def remove[K1, K2, V](map: Map[K1, K2, V])(k1: K1, k2: K2): F[Unit]

  def containsKey[K1, K2, V](map: Map[K1, K2, V])(k1: K1, k2: K2): F[Boolean]

  def containsValue[K1, K2, V](map: Map[K1, K2, V])(k1: K1, k2: K2, v: V): F[Boolean]

  def getAll[K1, K2, V](map: Map[K1, K2, V])(k1: K1): F[Predef.Map[K2, V]]

  def toNormalMap[K1, K2, V](map: Map[K1, K2, V]): F[Predef.Map[K1, Predef.Map[K2, V]]]

}
