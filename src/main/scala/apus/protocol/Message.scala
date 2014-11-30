package apus.protocol

import scala.xml.{Text, Null, UnprefixedAttribute, Elem}

/**
 * A Message stanza.
 * Created by Hao Chen on 2014/11/24.
 */
case class Message(override val xml: Elem) extends Stanza{

  import Message._

  require(verify(xml), "This xml is not a valid Message stanza")

  val to = toOpt.get

  override val label = Message.Label

  def msgType = MessageType(attr("type"))

  lazy val body: String = (xml \ "body").text

  def copy(from: Option[Jid] = this.fromOpt) = {
    val metaData = new UnprefixedAttribute("from", from.map(_.toString).map(Text(_)), Null)
    new Message(xml % metaData)
  }
}

object Message {

  import apus.util.XmlUtil._

  val Label: String = "message"

  /**
   * check whether this xml is a valid Message stanza
   * @param xml
   * @return
   */
  def verify(xml: Elem): Boolean = {
    xml.label == Label &&
      attr(xml, "to").flatMap( Jid.parse(_) ).isDefined &&
      (xml \ "body").isEmpty == false
  }
}

object MessageType extends Enumeration{

  val Chat = Value("chat")
  val Normal = Value("normal")
  val Group = Value("groupchat")
  val Headline = Value("headline")
  val Error = Value("error")

  def apply(str: String): Value = {
    values.find( _.toString == str.toLowerCase ).getOrElse(Chat)
  }

  def apply(strOpt: Option[String]): Value = apply(strOpt.orNull)
}