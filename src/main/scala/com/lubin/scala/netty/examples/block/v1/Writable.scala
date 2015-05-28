package com.lubin.scala.netty.examples.block.v1

import io.netty.buffer.ByteBuf

trait Writable extends Any {
  def write(out: ByteBuf): Unit
}
