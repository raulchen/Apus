package apus.protocol

import apus.util.XmlUtil

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/24.
 */
trait Stanza {

  def xml: Elem

  def label: String

  val idOpt = attr("id")

  val fromOpt = attr("from").flatMap( Jid.parse(_) )

  val toOpt = attr("to").flatMap( Jid.parse(_) )

  def attr(key: String): Option[String] = {
    XmlUtil.attr(xml, key)
  }

  override def toString: String = xml.toString
}


class UnknownStanza(override val xml: Elem) extends Stanza{

  override def label: String = xml.label
}

object Stanza{

  def apply(xml: Elem): Stanza = {
    xml match {
      case xml: Elem if Iq.verify(xml) => new Iq(xml)
      case xml: Elem if Presence.verify(xml) => new Presence(xml)
      case xml: Elem if Message.verify(xml) => new Message(xml)
      case _ => new UnknownStanza(xml)
    }
  }
}