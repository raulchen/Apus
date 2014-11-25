package apus.network

import apus.server.ServerConfig

import scala.collection.JavaConverters._
import java.net.InetAddress

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.group.{DefaultChannelGroup, ChannelGroup}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.{LineBasedFrameDecoder, DelimiterBasedFrameDecoder, Delimiters}
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.{SslContext, SslHandler}
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.netty.util.concurrent.{Future => NFuture, GlobalEventExecutor, GenericFutureListener}

/**
 * Created by Hao Chen on 2014/11/5.
 */
class TcpEndpoint(port: Int, config: ServerConfig) extends Endpoint{

  val channelInitializer = new ChannelInitializer[SocketChannel]() {

    override def initChannel(ch: SocketChannel): Unit = {
      val pipeline = ch.pipeline()
      pipeline.addLast("stringEncoder", new StringEncoder())
      pipeline.addLast("xmlEncoder", new XmlEncoder)

      pipeline.addLast("xmlFrameDecoder", new XmlFrameDecoder)
      pipeline.addLast("streamHandler", new StreamHandler(config))

    }
  }

  override def start(): Unit = {
    val bossGroup: EventLoopGroup = new NioEventLoopGroup(1)
    val workerGroup: EventLoopGroup = new NioEventLoopGroup()

    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .option(ChannelOption.SO_BACKLOG,Integer.valueOf(1024))
//        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(channelInitializer)

      b.bind(port).sync().channel().closeFuture().sync()
    }
    finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

  override def shutdown(): Unit = ???
}
