package apus.util

import scala.xml.Text

/**
 * Created by Hao Chen on 2014/11/19.
 */
object XmlImplicit {

  implicit def optStrToOptText(opt: Option[String]): Option[Text] = opt map { Text(_) }
}
