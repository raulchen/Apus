
import apus.protocol.Jid
import apus.util.Xml
import com.fasterxml.aalto.{AsyncXMLStreamReader, AsyncXMLInputFactory}
import com.fasterxml.aalto.evt.EventAllocatorImpl
import com.fasterxml.aalto.stax.InputFactoryImpl
import com.typesafe.scalalogging.LazyLogging

import scala.io.{StdIn, Source}
import scala.xml.pull.XMLEventReader

/**
 * Created by Hao Chen on 2014/11/15.
 */
object TempTest{

  class A extends LazyLogging{
    def f(): Unit ={
      logger.error({println("hehe"); "sfs" })
    }
  }

  def main(args: Array[String]) {
    val a = new A
    a.f
  }

}
