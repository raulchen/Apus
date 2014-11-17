package apus.network

import java.io.ByteArrayOutputStream
import java.util
import javax.xml.namespace.QName
import javax.xml.stream.events.{StartElement, XMLEvent}

import apus.protocol.{StreamEnd, StreamStart, XmppLabels, XmppNamespaces}
import com.fasterxml.aalto.{WFCException, AsyncXMLStreamReader}
import com.fasterxml.aalto.evt.EventAllocatorImpl
import com.fasterxml.aalto.stax.{InputFactoryImpl, OutputFactoryImpl}
import io.netty.buffer.ByteBuf
import io.netty.channel.{SimpleChannelInboundHandler, ChannelHandlerContext}
import io.netty.handler.codec.{ByteToMessageDecoder, MessageToMessageDecoder}

import scala.xml.XML

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
      case e:WFCException => ctx.disconnect() //TODO
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
class StreamHandler extends SimpleChannelInboundHandler[XMLEvent]{

  import apus.network.StreamHandler._

  var inStream = false
  var depth = 0
  val buf = new ByteArrayOutputStream(1024)
  val writer = xmlOutputFactory.createXMLEventWriter(buf)


  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    super.channelActive(ctx)
    //TODO create Actor
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    super.channelInactive(ctx)
    //TODO
  }

  private def isStreamHead(event: StartElement):Boolean ={
    if(XmppLabels.stream == event.getName.getLocalPart){
      val namepspace = event.getNamespaceURI("")
      if(XmppNamespaces.SERVER == namepspace ||
          XmppNamespaces.CLIENT == namepspace){
        true
      }
    }
    false
  }

  private def emit(msg: Any):Unit = {

  }

  override def channelRead0(ctx: ChannelHandlerContext, event: XMLEvent): Unit = {
    if(event.isStartDocument){
      depth = 0
      buf.reset
      return
    }

    if(event.isStartElement){
      depth+=1
      if(depth==1){
        //begin stream
        if(isStreamHead(event.asStartElement)){
          emit(StreamStart)
        }
      }
      else{
        writer.add(event)
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
          writer.flush()
          emit(XML.loadString(buf.toString))
          buf.reset()
        }
      }
    }
  }

}
