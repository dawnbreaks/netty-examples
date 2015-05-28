package com.lubin.scala.netty.examples.block

import java.util.concurrent.ConcurrentHashMap


class ConcurrentBlockStore[Key] extends BlockStore[Key] {

  import scala.collection.convert.WrapAsScala._

  val data: scala.collection.concurrent.Map[Key, Array[Byte]] = new ConcurrentHashMap[Key, Array[Byte]]

  override def apply(key: Key): Option[Array[Byte]] = data.get(key)

  override def add(key: Key, value: Array[Byte]): Unit = data.put(key, value)
}
