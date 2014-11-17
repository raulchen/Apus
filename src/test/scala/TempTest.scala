
import com.fasterxml.aalto.{AsyncXMLStreamReader, AsyncXMLInputFactory}
import com.fasterxml.aalto.evt.EventAllocatorImpl
import com.fasterxml.aalto.stax.InputFactoryImpl

import scala.io.{StdIn, Source}
import scala.xml.pull.XMLEventReader

/**
 * Created by Hao Chen on 2014/11/15.
 */
object TempTest {

  def main(args: Array[String]) {
    val factory = new InputFactoryImpl
    //  factory.setProperty(AsyncXMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    val allocator = EventAllocatorImpl.getDefaultInstance()

    val reader = factory.createAsyncXMLStreamReader()

    val data = "<stream:stream aa='aaa' xmlns='jabber:client' xmlns:stream=\"http://etherx.jabber.org/streams\"><b>dss</b></stream:stream>".getBytes("utf-8")
    reader.getInputFeeder.feedInput(data,0,data.length)

    while(reader.hasNext && reader.next != AsyncXMLStreamReader.EVENT_INCOMPLETE){
      val event = allocator.allocate(reader)
      if(event.isStartElement){
        println(event.asStartElement.getName.getLocalPart)
        println(event.asStartElement().getNamespaceURI("stream"))
      }
    }

    val moreData="</sdf>".getBytes
    reader.getInputFeeder.feedInput(moreData,0,moreData.length)
    while(reader.hasNext && reader.next != AsyncXMLStreamReader.EVENT_INCOMPLETE){
      val event = allocator.allocate(reader)
      println(event)
    }
  }

}
