package apus.util

import scala.xml.{Elem, Text}
import scala.language.implicitConversions

/**
 * Some implicit conversions for xml manipulation
 * Created by Hao Chen on 2014/11/19.
 */
object XmlImplicits {

  implicit def optStrToOptText(opt: Option[String]): Option[Text] = opt map { Text(_) }

//  implicit def wrapXml(xml: Elem): WrappedXml = new WrappedXml(xml)
//
//  implicit def unwrapXml(wrapped: WrappedXml): Elem = wrapped.self
}

//class WrappedXml(val self: Elem){
//
//  def attr(attr: String): Option[String] = {
//    val nodeSeq = self \ s"@${attr}"
//    if(nodeSeq.length==1){
//      Some(nodeSeq.text)
//    }
//    else{
//      None
//    }
//  }
//}