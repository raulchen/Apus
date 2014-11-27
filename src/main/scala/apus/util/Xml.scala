package apus.util

import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

import scala.xml.{Elem, SAXParser}
import scala.xml.factory.XMLLoader

/**
 * Use this class for XML parsing instead of scala.xml.XML,
 * in case that it finds SAXParserFactory from Aalto. <br/>
 *
 * Created by Hao Chen on 2014/11/24.
 */
object Xml extends XMLLoader[Elem]{

  override def parser: SAXParser = {
    val f = new com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl
    f.setNamespaceAware(false)
    f.newSAXParser()
  }

  /**
   * parse a String to scala.xml.Elem
   * @param str
   * @return
   */
  def apply(str: String) = {
    loadString(str)
  }

  /**
   * parse a InputStream to scala.xml.ELem
   * @param is
   * @return
   */
  def apply(is: InputStream) = {
    load(is)
  }
}
