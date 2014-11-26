package apus.protocol

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/24.
 */
class Presence(override val xml: Elem) extends Stanza{

  import Presence._

  require(verify(xml), "This xml is not a valid Presence stanza")

  override def label: String = "presence"
}

object Presence {

  import apus.util.XmlUtil._

  val Label = "presence"

  /**
   * check whether this xml is a valid Message stanza
   * @param xml
   * @return
   */
  def verify(xml: Elem): Boolean = {
    xml.label == Label
  }
}