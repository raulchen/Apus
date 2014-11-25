package apus.protocol

/**
 * Created by Hao Chen on 2014/11/17.
 */
case class Jid(node: Option[String], domain: String, resource: Option[String] = None) {

  def bare: Jid ={
    if(resource.isDefined){
      copy(node, domain, None)
    }
    else{
      this
    }
  }

  override def toString: String = {
    val sb = new StringBuilder
    node.foreach{
      sb.append(_).append("@")
    }
    sb.append(domain)
    resource.foreach{
      sb.append("/").append(_)
    }
    sb.toString
  }
}

object Jid{

  private val validChars = "[^@/]+"
  private val pattern = s"((${validChars})@)?(${validChars})(/(${validChars}))?".r

  /**
   * parse Jid
   * @param jid
   * @return
   */
  def apply(jid: String): Jid = {
    jid match {
      case pattern(_, node, domain, _, resource) => new Jid(Option(node), domain, Option(resource))
      case _ => throw new IllegalArgumentException("Jid must be of format [node@]domain[/resource]")
    }
  }

  def apply(jidOpt: Option[String]): Option[Jid] = {
    jidOpt.map(apply(_))
  }

  def parse(jid: String): Option[Jid] ={
    try{
      Some(apply(jid))
    }
    catch {
      case e => None
    }
  }
}