package apus.network

import java.io.{IOException, ByteArrayOutputStream}
import java.util
import javax.xml.stream.events.{StartElement, XMLEvent}

import akka.actor.{PoisonPill, ActorRef}
import akka.event.Logging
import apus.protocol.{StreamStart, XmppNamespaces}
import apus.server.ServerRuntime
import apus.util.Xml
import com.fasterxml.aalto.evt.EventAllocatorImpl
import com.fasterxml.aalto.stax.{InputFactoryImpl, OutputFactoryImpl}
import com.fasterxml.aalto.{AsyncXMLStreamReader, WFCException}
import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelInboundHandlerAdapter, ChannelHandlerContext, SimpleChannelInboundHandler}
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
}


object StreamHandler{
  private val xmlOutputFactory = new OutputFactoryImpl
}

/**
 * handle incoming XMLEvent, and send StreamStart/StreamEnd/Stanzas to Session actor.
 */
class StreamHandler(runtime: ServerRuntime) extends SimpleChannelInboundHandler[XMLEvent]{

  import apus.network.StreamHandler._

  val InitialBufSize = 512
  val MaxBufSizeToTrim = 5 * 1024
  val MaxSizePerStanza = 20 * 1024

  val log = Logging(runtime.actorSystem, this.getClass)

  var depth = 0
  var buf = new ByteArrayOutputStream(InitialBufSize)
  var writer = xmlOutputFactory.createXMLEventWriter(buf)

  var session: ActorRef = _

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    ctx.writeAndFlush("""<?xml version="1.0" encoding="UTF-8"?>""")
    session = runtime.createSession(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    //end session actor
    session ! PoisonPill
  }

  private def isStreamHead(event: StartElement): Boolean ={
    if("stream" == event.getName.getLocalPart){
      val namespace = event.getNamespaceURI("")
      if(XmppNamespaces.SERVER == namespace ||
          XmppNamespaces.CLIENT == namespace){
        true
      }
      else{
        false
      }
    }
    else {
      false
    }
  }

  private def emit(msg: AnyRef): Unit = {
//    log.debug("emit {}", msg)
    session ! msg
  }

  private def endElem(): Elem ={
    writer.flush()
    writer.close()
    writer = xmlOutputFactory.createXMLEventWriter(buf)

    val elem = Xml(String.valueOf(buf))

    buf.reset()
    if(buf.size > MaxBufSizeToTrim){
      buf = new ByteArrayOutputStream(InitialBufSize)
    }

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
        depth += 1
        writer.add(event)
        if(buf.size > MaxSizePerStanza){
          throw new IOException(s"Stanza too large: ${buf.size}")
        }
      }
    }
    else if(event.isEndElement){
      depth -= 1
      if(depth == 0){
        //end stream
        ctx.close()
      }
      else{
        writer.add(event)
        if(depth == 1){
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


class InboundExceptionHandler(runtime: ServerRuntime) extends ChannelInboundHandlerAdapter{

  val log = Logging(runtime.actorSystem, this.getClass)

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause match {
      case e: WFCException => {
        log.warning("Invalid xml format from client")
        ctx.close()
      }
      case e: IOException => {
        if(e.getMessage == "An existing connection was forcibly closed by the remote host"){
          log.debug(e.getMessage)
        }
        else{
          log.error(e, "IOException caught, close channel.")
        }
        ctx.close()
      }
      case _ => super.exceptionCaught(ctx, cause)
    }
  }
}