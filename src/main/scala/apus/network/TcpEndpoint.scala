package apus.network

import akka.event.Logging
import apus.server.ServerRuntime

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

import scala.util.control.NonFatal

/**
 * Created by Hao Chen on 2014/11/5.
 */
class TcpEndpoint(port: Int, runtime: ServerRuntime) extends Endpoint{

  val log = Logging(runtime.actorSystem().eventStream, this.getClass.getCanonicalName)

  val channelInitializer = new ChannelInitializer[SocketChannel]() {

    override def initChannel(ch: SocketChannel): Unit = {
      val pipeline = ch.pipeline()
      pipeline.addLast("stringEncoder", new StringEncoder())
      pipeline.addLast("xmlEncoder", new XmlEncoder)

      pipeline.addLast("xmlFrameDecoder", new XmlFrameDecoder)
      pipeline.addLast("streamHandler", new StreamHandler(runtime))

      pipeline.addLast("exceptionHandler", new InboundExceptionHandler(runtime))
    }
  }

  var ch: Channel = null
  var bossGroup: EventLoopGroup = null
  var workerGroup: EventLoopGroup = null

  override def start(): Unit = {
    bossGroup = new NioEventLoopGroup()
    workerGroup = new NioEventLoopGroup()

    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .option(ChannelOption.SO_BACKLOG,Integer.valueOf(1024))
//        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(channelInitializer)

      ch = b.bind(port).sync().channel()
    }
    catch {
      case NonFatal(e) => {
        log.error("Fail to start TCP endpoint: {}", e.getMessage)
        shutdown()
      }
    }
  }

  override def shutdown(): Unit = {
    if(ch != null) {
      ch.close()
    }
    if(bossGroup != null) {
      bossGroup.shutdownGracefully()
    }
    if(workerGroup != null){
      workerGroup.shutdownGracefully()
    }
  }
}
