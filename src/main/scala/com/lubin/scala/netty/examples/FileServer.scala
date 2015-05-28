package com.lubin.scala.netty.examples

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFuture, ChannelHandler, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.{ByteToMessageDecoder, MessageToByteEncoder}
import io.netty.util.CharsetUtil

case class FileRequest(path: String)

case class FileResponse(path: String, length: Long, data: ByteBuffer)


class FileRequestEncoder extends MessageToByteEncoder[FileRequest] with Logging  {
  private[this] val Encoding = CharsetUtil.UTF_8

  override def encode(ctx: ChannelHandlerContext, msg: FileRequest, out: ByteBuf): Unit = {
    logger.info(s"FileRequestEncoder|path ${msg.path}")
    writeString(out, msg.path)
  }

  private def writeString(out: ByteBuf, data: String) = {
    out.writeLong(data.length)
    out.writeBytes(data.getBytes(Encoding))
  }
}


class FileRequestDecoder extends ByteToMessageDecoder with Logging {
  override def decode(ctx: ChannelHandlerContext, inBuf: ByteBuf, out: util.List[AnyRef]): Unit = {
    if(inBuf.readableBytes() <= 4){
      return
    }

    val readerIndex = inBuf.readerIndex()
    val pathLength = inBuf.readLong()
    if (inBuf.readableBytes() >= pathLength) {
      val pathBytes = new Array[Byte](pathLength.asInstanceOf[Int])
      inBuf.readBytes(pathBytes)
      val path = new String(pathBytes, "UTF-8")
      out.add(FileRequest(path))
      logger.info(s"FileRequestDecoder|path=$path")
      return
    }
    //not enough data
    inBuf.readerIndex(readerIndex)
  }
}


class FileResponseEncoder extends MessageToByteEncoder[FileResponse] with Logging  {
  private[this] val Encoding = CharsetUtil.UTF_8

  override def encode(ctx: ChannelHandlerContext, msg: FileResponse, out: ByteBuf): Unit = {
    logger.info(s"Encoding path ${msg.path}")
    writeString(out, msg.path)
    out.writeLong(msg.length)
    out.writeBytes(msg.data)
  }

  private def writeString(out: ByteBuf, data: String) = {
    out.writeLong(data.length)
    out.writeBytes(data.getBytes(Encoding))
  }
}

class FileResponseDecoder extends ByteToMessageDecoder with Logging  {
  private[this] val Encoding = CharsetUtil.UTF_8

  override def decode(ctx: ChannelHandlerContext, inBuf: ByteBuf, out: util.List[AnyRef]): Unit = {
    if(inBuf.readableBytes() <= 4){
      return
    }

    val readerIndex = inBuf.readerIndex()
    val pathLength = inBuf.readLong()
    if (inBuf.readableBytes() >= pathLength + 4) {
      val pathBytes = new Array[Byte](pathLength.asInstanceOf[Int])
      inBuf.readBytes(pathBytes)
      val path = new String(pathBytes, "UTF-8")
      val dataLen = inBuf.readLong()
      if (inBuf.readableBytes() >= dataLen){
        val dataBytes = new Array[Byte](dataLen.asInstanceOf[Int])
        inBuf.readBytes(dataBytes)
        val fileResponse = new FileResponse(path, dataLen, ByteBuffer.wrap(dataBytes))
        out.add(fileResponse)
        logger.info(s"FileResponseDecoder|path=$path")
        return
      }
    }

    //not enough data
    inBuf.readerIndex(readerIndex)
  }

  private def writeString(out: ByteBuf, data: String) = {
    out.writeLong(data.length)
    out.writeBytes(data.getBytes(Encoding))
  }
}

class FileServerHandler extends ChannelInboundHandlerAdapter with Logging {
  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    val req = msg.asInstanceOf[FileRequest]
    val file: RandomAccessFile = new RandomAccessFile(req.path.trim, "r")
    val length = file.length()
    val channel = file.getChannel()
    val data = channel.map(FileChannel.MapMode.READ_ONLY, 0, length)

    logger.info(s"Serving path ${req.path}")
    val future = ctx.writeAndFlush(FileResponse(req.path, length, data))

    future.addListener((f: ChannelFuture) => ctx.close())
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.error("FileServerHandler|exceptionCaught", cause)
    ctx.close()
  }
}

object FileServer extends Server {
  override def port = 8888
  override def workerHandlers(): List[ChannelHandler] =
    List(
      new FileRequestDecoder,
      new FileResponseEncoder,
      new FileServerHandler
    )
}
