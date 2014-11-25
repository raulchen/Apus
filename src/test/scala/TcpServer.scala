import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, BufferedWriter}
import java.net.Socket
import java.util.Scanner
import javax.net.ssl
import javax.net.ssl.{ SSLSocket, SSLSocketFactory}

import apus.network.TcpEndpoint
import apus.server.DefaultXmppServer
import io.netty.bootstrap.Bootstrap
import io.netty.channel.{ChannelFuture, ChannelHandlerContext, SimpleChannelInboundHandler, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.{FixedLengthFrameDecoder, LineBasedFrameDecoder}
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.util.{InsecureTrustManagerFactory, SelfSignedCertificate}

import scala.io.StdIn
import scala.util.control.Breaks

/**
 * Created by Hao Chen on 2014/11/6.
 */
object TcpServer extends App{

  DefaultXmppServer.startUp

}

object TcpClient{

  def main(args: Array[String]) {

    val sslContext = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE)

    val group = new NioEventLoopGroup()

    try {
      val b = new Bootstrap()
      b.group(group)
        .channel(classOf[NioSocketChannel])
        .handler(new ChannelInitializer[SocketChannel]() {
          override def initChannel(ch: SocketChannel): Unit = {
            val pipeline = ch.pipeline()
            pipeline.addLast(sslContext.newHandler(ch.alloc(),"localhost",5222))
            pipeline.addLast(new FixedLengthFrameDecoder(10))
            pipeline.addLast(new StringDecoder())
            pipeline.addLast(new StringEncoder())

            pipeline.addLast(new SimpleChannelInboundHandler[String]() {
              override def channelRead0(ctx: ChannelHandlerContext, msg: String): Unit = {
                println(msg)
              }

              override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
                cause.printStackTrace()
                ctx.close
              }
            })
          }
        })


        var lastWriteFuture: ChannelFuture = null
        val ch = b.connect("localhost",23333).channel()

        Breaks.breakable{
          while(true){
            val line = StdIn.readLine()
            if(line == ""){
              Breaks.break
            }

            lastWriteFuture = ch.writeAndFlush(line+"\n")

            if("bye" == line.toLowerCase){
              ch.closeFuture().sync()
              Breaks.break()
            }
          }

          if(lastWriteFuture!=null){
            lastWriteFuture.sync()
          }
        }
    }
    finally{
      group.shutdownGracefully()
    }

  }
}