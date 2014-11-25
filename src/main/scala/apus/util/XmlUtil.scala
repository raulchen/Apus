package apus.util

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/24.
 */
object XmlUtil {

  def attr(xml: Elem, key: String): Option[String] = {
    xml.attribute(key).map(_.text)
  }
}
