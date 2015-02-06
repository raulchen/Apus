package apus.network

import java.util

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

import scala.xml.Elem

/*
 * Created by Hao Chen on 2014/11/24.
 */

class XmlEncoder extends MessageToMessageEncoder[Elem]{

  val streamEnd = "</stream:stream>"

  override def encode(ctx: ChannelHandlerContext, msg: Elem, out: util.List[AnyRef]): Unit = {
    var str = scala.xml.Utility.trim(msg).toString()
    if(str.endsWith(streamEnd)){
      str = str.substring(0, str.length - streamEnd.length)
    }
    out.add(str)
  }
}
