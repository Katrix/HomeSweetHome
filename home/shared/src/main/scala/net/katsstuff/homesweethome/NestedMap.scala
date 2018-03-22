package net.katsstuff.homesweethome

import scala.collection.generic.{Growable, Shrinkable}
import scala.collection.mutable

/**
  * A small wrapper around a nested mutable map.
  * @tparam A The first key type
  * @tparam B The second key type
  * @tparam C The value type
  */
class NestedMap[A, B, C] private (internalMap: mutable.Map[A, mutable.Map[B, C]], create: () => mutable.Map[B, C])
    extends Growable[(A, B, C)]
    with Shrinkable[(A, B)] {

  private def getNestedMap(k1: A): mutable.Map[B, C] = internalMap.getOrElseUpdate(k1, create())

  def get(k1: A, k2: B): Option[C] = internalMap.get(k1).flatMap(_.get(k2))

  def getOrElse[C1 >: C](k1: A, k2: B, default: => C1): C1 = getNestedMap(k1).getOrElse(k2, default)

  def getOrElseUpdate(k1: A, k2: B, default: => C): C = getNestedMap(k1).getOrElseUpdate(k2, default)

  def getAll(k1: A): Map[B, C]         = getNestedMap(k1).toMap
  def toNormalMap:   Map[A, Map[B, C]] = internalMap.map { case (k, v) => k -> v.toMap }.toMap

  def apply(k1: A, k2: B): C = internalMap(k1)(k2)

  def contains(k1: A, k2: B):    Boolean = internalMap.get(k1).exists(_.contains(k2))
  def isDefinedAt(k1: A, k2: B): Boolean = contains(k1, k2)

  def applyOrElse[A1 <: A, B1 <: B, C1 >: C](k1: A1, k2: B1, default: (A1, B1) => C1): C1 =
    getOrElse(k1, k2, default(k1, k2))

  def put(k1: A, k2: B, v: C):    Option[C] = getNestedMap(k1).put(k2, v)
  def update(k1: A, k2: B, v: C): Option[C] = put(k1, k2, v)

  def +=(t: (A, B, C)): this.type = {
    val (k1, k2, v) = t
    put(k1, k2, v)
    this
  }

  def remove(k1: A, k2: B): Option[C] = getNestedMap(k1).remove(k2)

  def -=(t: (A, B)): this.type = {
    val (k1, k2) = t
    getNestedMap(k1).remove(k2)
    this
  }

  def clear(): Unit = internalMap.clear()
  def iterator: Iterator[(A, B, C)] =
    for {
      (k1, innerMap) <- internalMap.iterator
      (k2, v)        <- innerMap.iterator
    } yield (k1, k2, v)
}
object NestedMap {
  def apply[A, B, C](
      internalMap: mutable.Map[A, mutable.Map[B, C]],
      create: () => mutable.Map[B, C]
  ): NestedMap[A, B, C] = new NestedMap(internalMap, create)
}
