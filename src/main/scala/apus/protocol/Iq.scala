package apus.protocol

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/24.
 */
class Iq(override val xml: Elem) extends Stanza{

  override def label: String = "iq"

  val iqType = IqType(attr("type"))

}

object IqType extends Enumeration{

  val GET = Value("get")
  val SET = Value("set")
  val RESULT = Value("result")
  val ERROR = Value("error")
  val UNKNOWN = Value("unknown")

  def apply(str: String): Value = values.find( _.toString == str.toLowerCase ).getOrElse(UNKNOWN)

  def apply(strOpt: Option[String]): Value = strOpt.map(apply(_)).get
}