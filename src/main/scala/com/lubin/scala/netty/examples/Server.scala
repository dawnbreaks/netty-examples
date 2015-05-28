package com.lubin.scala.netty.examples

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{ChannelHandler, ChannelOption, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LoggingHandler

trait Server extends App with Logging{
  def port: Int = 8889
  val bossGroup = new NioEventLoopGroup()
  val workerGroup = new NioEventLoopGroup()

  try {
    val childHandler = new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit =
        ch.pipeline().addLast(workerHandlers(): _*).addLast(new LoggingHandler())
    }

    val bootstrap = new ServerBootstrap().
      group(bossGroup, workerGroup).
      channel(classOf[NioServerSocketChannel]).
      childHandler(childHandler).
      option(ChannelOption.SO_BACKLOG, new Integer(128)).
      childOption(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)

    val future = bootstrap.bind(port).sync()
    logger.info(s"Server ready for connections on port $port")

    future.channel().closeFuture().sync()
  } finally {
    workerGroup.shutdownGracefully()
    bossGroup.shutdownGracefully()
  }

  def workerHandlers(): List[ChannelHandler]
}
