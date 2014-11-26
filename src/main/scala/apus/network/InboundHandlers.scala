package apus.network

import java.io.ByteArrayOutputStream
import java.util
import javax.xml.stream.events.{StartElement, XMLEvent}

import akka.actor.ActorRef
import akka.event.Logging
import apus.protocol.{StreamEnd, StreamStart, XmppLabels, XmppNamespaces}
import apus.server.ServerConfig
import apus.session.Session
import apus.util.Xml
import com.fasterxml.aalto.evt.EventAllocatorImpl
import com.fasterxml.aalto.stax.{InputFactoryImpl, OutputFactoryImpl}
import com.fasterxml.aalto.{AsyncXMLStreamReader, WFCException}
import com.typesafe.scalalogging.{StrictLogging, LazyLogging}
import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.ByteToMessageDecoder

import scala.xml.Elem

/*
 * Created by Hao Chen on 2014/11/15.
 */

private object XmlFrameDecoder{

  val factory = new InputFactoryImpl
  val allocator = EventAllocatorImpl.getDefaultInstance

}

/**
 * Convert input bytes to XmlEvent
 */
class XmlFrameDecoder extends ByteToMessageDecoder{

  import apus.network.XmlFrameDecoder._

  val reader = factory.createAsyncXMLStreamReader

  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit = {
    val chunk = new Array[Byte](in.readableBytes())
    in.readBytes(chunk)
    reader.getInputFeeder.feedInput(chunk, 0, chunk.length)

    while(reader.hasNext && reader.next != AsyncXMLStreamReader.EVENT_INCOMPLETE){
      out.add(allocator.allocate(reader))
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause match {
      case e: WFCException => ctx.close() //TODO
      case _ => super.exceptionCaught(ctx, cause)
    }
  }
}



private object StreamHandler{
  val xmlOutputFactory = new OutputFactoryImpl
}

/**
 * handle incoming XMLEvent, and send StreamStart/StreamEnd/Stanzas to Session actor.
 */
class StreamHandler(config: ServerConfig) extends SimpleChannelInboundHandler[XMLEvent]{

  import apus.network.StreamHandler._

  val log = Logging(config.actorSystem.eventStream, this.getClass)

  var inStream = false
  var depth = 0
  val buf = new ByteArrayOutputStream(512)
  var writer = xmlOutputFactory.createXMLEventWriter(buf)

  var session: Option[ActorRef] = None

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    super.channelActive(ctx)
    ctx.writeAndFlush("""<?xml version="1.0" encoding="UTF-8"?>""")
    session = Some(config.actorSystem.actorOf(Session.props(ctx,config)))
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    super.channelInactive(ctx)
    //TODO
  }

  private def isStreamHead(event: StartElement): Boolean ={
    if(XmppLabels.STREAM == event.getName.getLocalPart){
      val namespace = event.getNamespaceURI("")
      if(XmppNamespaces.SERVER == namespace ||
          XmppNamespaces.CLIENT == namespace){
        return true
      }
    }
    return false
  }

  private def emit(msg: AnyRef): Unit = {
    log.debug("emit {}", msg)
    session.foreach( _ ! msg )
  }

  private def endElem(): Elem ={
    writer.flush()
    writer.close()
    writer = xmlOutputFactory.createXMLEventWriter(buf)
    val elem = Xml(String.valueOf(buf))
    buf.reset
    elem
  }

  override def channelRead0(ctx: ChannelHandlerContext, event: XMLEvent): Unit = {
    if(event.isStartDocument || event.isEndDocument){
      return
    }
    if(event.isStartElement){
      if(isStreamHead(event.asStartElement)){
        depth=1
        emit(StreamStart)
      }
      else{
        depth+=1
        writer.add(event)
        //TODO check buf size
      }
    }
    else if(event.isEndElement){
      depth-=1
      if(depth==0){
        //end stream
        emit(StreamEnd)
      }
      else{
        writer.add(event)
        if(depth==1){
          //emit stanza
          emit(endElem())
        }
      }
    }
    else{
      writer.add(event)
    }
  }

}
