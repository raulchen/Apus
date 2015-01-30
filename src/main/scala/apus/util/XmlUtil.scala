package apus.util

import scala.xml.Elem

/**
 * A utility class for Xml
 * Created by Hao Chen on 2014/11/24.
 */
object XmlUtil {

  /**
   * get an attribute from the xml
   * @return
   */
  def attr(xml: Elem, key: String): Option[String] = {
    xml.attribute(key).map(_.text)
  }
}
