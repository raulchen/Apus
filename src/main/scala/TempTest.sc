import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml._

val myXml = <myTag/> % Attribute(None, "name", Text("value"), Null)

val xml = <root><a/><b/><c/></root>

val rr = new RewriteRule {
  override def transform(n: Node): Seq[Node] = n match {
    case elem : Elem => elem % Attribute(None, "name", Text("value"), Null) toSeq
    case other => other
  }
}

val rt = new RuleTransformer(rr)



