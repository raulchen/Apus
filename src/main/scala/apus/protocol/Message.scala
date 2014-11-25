package apus.protocol

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/24.
 */
class Message (override val xml: Elem) extends Stanza{

  override def label: String = "message"
}

