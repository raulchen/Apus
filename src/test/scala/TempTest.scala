
import apus.protocol.{Message, Jid}
import apus.util.Xml
import com.fasterxml.aalto.{AsyncXMLStreamReader, AsyncXMLInputFactory}
import com.fasterxml.aalto.evt.EventAllocatorImpl
import com.fasterxml.aalto.stax.InputFactoryImpl
import com.typesafe.scalalogging.LazyLogging

import scala.io.{StdIn, Source}
import scala.xml.XML
import scala.xml.pull.XMLEventReader

/**
 * Created by Hao Chen on 2014/11/15.
 */
object TempTest{

  class A(val x: Int) {

    def x2 = x*x
  }

  def main(args: Array[String]) {
    val raw = """<message
                    to='romeo@example.net'
                    from='juliet@example.com/balcony'
                    type='chat'
                    xml:lang='en'>
                  <body>Wherefore art thou, Romeo?</body>
                </message>"""
    val xml = Xml(raw)
    val msg = new Message(xml)

    println(msg.copy().body)
  }

}
