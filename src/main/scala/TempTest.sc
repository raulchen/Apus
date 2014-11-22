import scala.xml.Text
implicit def optStrToOptText(opt: Option[String]) = opt map {
  Text(_)
}

val checked:Option[String] = Some("hehe")

val xml = <input checked={checked} />
