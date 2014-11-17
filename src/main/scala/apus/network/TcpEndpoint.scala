package apus.network
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
class TcpEndpoint(port: Int, sslContext: SslContext) extends Endpoint{

  override def start(): Unit = {
    val bossGroup: EventLoopGroup = new NioEventLoopGroup(1)
    val workerGroup: EventLoopGroup = new NioEventLoopGroup()

    try{
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new Initializer(sslContext))

      b.bind(port).sync().channel().closeFuture().sync()
    }
    finally{
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

  override def shutdown(): Unit = ???
}

class Initializer(sslContext: SslContext) extends ChannelInitializer[SocketChannel]{

  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline()
    // Add SSL handler first to encrypt and decrypt everything.
    // In this example, we use a bogus certificate in the server side
    // and accept any invalid certificates in the client side.
    // You will need something more complicated to identify both
    // and server in the real world.
    pipeline.addLast(sslContext.newHandler(ch.alloc()))

    pipeline.addLast(new XmlFrameDecoder)
    pipeline.addLast(new StreamHandler)

  }
}
//
//object SecureChatServerHandler{
//  val channels: ChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
//}
//
//class SecureChatServerHandler extends SimpleChannelInboundHandler[String] {
//
//  import SecureChatServerHandler._
//
//  override def channelActive(ctx: ChannelHandlerContext): Unit = {
//
//    ctx.pipeline().get(classOf[SslHandler]).handshakeFuture().addListener(
//
//      new GenericFutureListener[NFuture[Channel]] {
//
//        override def operationComplete(future: NFuture[Channel]): Unit = {
//          val hostName=InetAddress.getLocalHost.getHostName
//          ctx.writeAndFlush(s"Welcome to ${hostName} secure chat service!\n")
//          val cipherSuit=ctx.pipeline().get(classOf[SslHandler]).engine().getSession().getCipherSuite
//          ctx.writeAndFlush(s"Your session is protected by ${cipherSuit} cipher suite.\n")
//          channels.add(ctx.channel())
//        }
//      }
//    )
//  }
//
//  override def channelRead0(ctx: ChannelHandlerContext, msg: String): Unit = {
//
//    channels.asScala.foreach( c => {
//        if(c != ctx.channel()){
//          c.writeAndFlush(s"[${ctx.channel().remoteAddress()}] ${msg}\n")
//        }
//        else{
//          c.writeAndFlush(s"[You] ${msg}\n")
//        }
//        if("bye" == msg.toLowerCase){
//          ctx.close()
//        }
//      }
//    )
//  }
//
//  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
//    cause.printStackTrace()
//    ctx.close()
//  }
//}
