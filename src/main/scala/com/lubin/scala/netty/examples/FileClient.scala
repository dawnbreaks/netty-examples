package com.lubin.scala.netty.examples

import io.netty.channel.{ChannelHandler, ChannelHandlerContext, ChannelInboundHandlerAdapter}

object FileClient extends Client with Logging {
  override def port: Int = 8888
  override def pipeline: List[ChannelHandler] = List(
    new FileRequestEncoder,
    new FileResponseDecoder,
    new ChannelInboundHandlerAdapter with  Logging{
      override def channelActive(ctx: ChannelHandlerContext): Unit = {
        ctx.writeAndFlush(new FileRequest("D:/nokiaE7.eml"))
      }

      override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
        val fileRes = msg.asInstanceOf[FileResponse]
        logger.info(s"path=${fileRes.path}|length=${fileRes.length}|data=\n" + new String(fileRes.data.array(), "UTF-8"))
        ctx.close()
      }
    }
  )

}
