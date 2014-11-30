
import java.io.File
import java.util

import apus.protocol.{ServerResponses, Message, Jid}
import apus.session.SessionState
import apus.util.Xml
import com.fasterxml.aalto.{AsyncXMLStreamReader, AsyncXMLInputFactory}
import com.fasterxml.aalto.evt.EventAllocatorImpl
import com.fasterxml.aalto.stax.InputFactoryImpl

import scala.collection.JavaConverters._
import scala.io.{StdIn, Source}
import scala.xml.XML
import scala.xml.pull.XMLEventReader
import scala.concurrent.duration._

/**
 * Created by Hao Chen on 2014/11/15.
 */
object TempTest{

  class A(val x: Int) {

    def x2 = x*x
  }

  def main(args: Array[String]) {
    XML.loadString("<a/>")
  }

}
