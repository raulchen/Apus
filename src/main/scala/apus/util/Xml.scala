package apus.util

import javax.xml.parsers.SAXParserFactory

import scala.xml.{Elem, SAXParser}
import scala.xml.factory.XMLLoader

/**
 * Created by Hao Chen on 2014/11/24.
 */
object Xml extends XMLLoader[Elem]{

  override def parser: SAXParser = {
    val f = new com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl
    f.setNamespaceAware(false)
    f.newSAXParser()
  }

  def apply(str: String) = {
    loadString(str)
  }
}
