

/**
 * Created by Hao Chen on 2014/11/6.
 */
object TestServer extends App{

//  DefaultXmppServer.startUp()
//  StdIn.readLine()
//  DefaultXmppServer.shutDown()
}
//
//object TcpClient{
//
//  def main(args: Array[String]) {
//
//    val sslContext = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE)
//
//    val group = new NioEventLoopGroup()
//
//    try {
//      val b = new Bootstrap()
//      b.group(group)
//        .channel(classOf[NioSocketChannel])
//        .handler(new ChannelInitializer[SocketChannel]() {
//          override def initChannel(ch: SocketChannel): Unit = {
//            val pipeline = ch.pipeline()
//            pipeline.addLast(sslContext.newHandler(ch.alloc(),"localhost",5222))
//            pipeline.addLast(new FixedLengthFrameDecoder(10))
//            pipeline.addLast(new StringDecoder())
//            pipeline.addLast(new StringEncoder())
//
//            pipeline.addLast(new SimpleChannelInboundHandler[String]() {
//              override def channelRead0(ctx: ChannelHandlerContext, msg: String): Unit = {
//                println(msg)
//              }
//
//              override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
//                cause.printStackTrace()
//                ctx.close
//              }
//            })
//          }
//        })
//
//
//        var lastWriteFuture: ChannelFuture = null
//        val ch = b.connect("localhost",23333).channel()
//
//        Breaks.breakable{
//          while(true){
//            val line = StdIn.readLine()
//            if(line == ""){
//              Breaks.break
//            }
//
//            lastWriteFuture = ch.writeAndFlush(line+"\n")
//
//            if("bye" == line.toLowerCase){
//              ch.closeFuture().sync()
//              Breaks.break()
//            }
//          }
//
//          if(lastWriteFuture!=null){
//            lastWriteFuture.sync()
//          }
//        }
//    }
//    finally{
//      group.shutdownGracefully()
//    }
//
//  }
//}