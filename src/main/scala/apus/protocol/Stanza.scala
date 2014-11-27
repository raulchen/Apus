package apus.protocol

import apus.util.XmlUtil

import scala.xml.Elem

/**
 * A trait for XMPP stanza
 * Created by Hao Chen on 2014/11/24.
 */
trait Stanza {

  /**
   * the xml representation of this stanza
   * @return
   */
  def xml: Elem

  def label: String

  val idOpt: Option[String] = attr("id")

  val fromOpt: Option[Jid] = attr("from").flatMap( Jid.parse(_) )

  val toOpt: Option[Jid] = attr("to").flatMap( Jid.parse(_) )

  /**
   * find an attribute
   * @param key
   * @return
   */
  def attr(key: String): Option[String] = {
    XmlUtil.attr(xml, key)
  }

  override def toString: String = xml.toString
}

/**
 * A unknown stanza that is not Iq, Presence, or Message
 * @param xml
 */
class UnknownStanza(override val xml: Elem) extends Stanza{

  override def label: String = xml.label
}

object Stanza{

  /**
   * parse a xml to correct stanza
   * @param xml
   * @return
   */
  def apply(xml: Elem): Stanza = {
    xml match {
      case xml: Elem if Iq.verify(xml) => new Iq(xml)
      case xml: Elem if Presence.verify(xml) => new Presence(xml)
      case xml: Elem if Message.verify(xml) => new Message(xml)
      case _ => new UnknownStanza(xml)
    }
  }
}