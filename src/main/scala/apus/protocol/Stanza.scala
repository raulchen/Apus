package apus.protocol

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/24.
 */
trait Stanza {

  def xml: Elem

  def label: String

  val id = attr("id")

  val from = Jid(attr("from"))

  val to = Jid(attr("to"))

  def attr(key: String): Option[String] = {
    xml.attribute(key).map(_.text)
  }

  override def toString: String = xml.toString
}

class UnknownStanza(override val xml: Elem) extends Stanza{

  override def label: String = xml.label
}

object Stanza{

  def apply(xml: Elem): Stanza = {
    xml.label match {
      case "iq" => new Iq(xml)
      case "presence" => new Presence(xml)
      case "message" => new Message(xml)
      case _ => new UnknownStanza(xml)
    }
  }
}