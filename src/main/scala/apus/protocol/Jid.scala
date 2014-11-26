package apus.protocol

import scala.util.control.NonFatal

/**
 * Created by Hao Chen on 2014/11/17.
 */
case class Jid(nodeOpt: Option[String],
               domain: String,
               resourceOpt: Option[String]) {

  def node: String = nodeOpt.get

  //  def resource: String = resourceOpt.getOrElse("")

  def bare: Jid = {
    if (resourceOpt.isDefined) {
      copy(nodeOpt, domain, None)
    }
    else {
      this
    }
  }

  override def toString: String = {
    val sb = new StringBuilder
    nodeOpt.foreach {
      sb.append(_).append("@")
    }
    sb.append(domain)
    resourceOpt.foreach {
      sb.append("/").append(_)
    }
    sb.toString
  }
}

object Jid {

  private val validChars = "[^@/]+"
  private val pattern = s"((${validChars})@)?(${validChars})(/(${validChars}))?".r

  /**
   * parse string to Jid
   * @param str
   * @param requireNode
   * @return
   */
  def apply(str: String, requireNode: Boolean = true): Jid = {
    str match {
      case pattern(_, node, domain, _, resource) => {
        if (requireNode) {
          require(node != null, "Jid must have a node")
        }
        new Jid(Option(node), domain, Option(resource))
      }
      case _ => throw new IllegalArgumentException("Jid must be of format [node@]domain[/resource]")
    }
  }

  def parse(str: String, requireNode: Boolean = true): Option[Jid] = {
    try {
      if (str == null || str.isEmpty) {
        None
      }
      else {
        Some(apply(str, requireNode))
      }
    }
    catch {
      case NonFatal(e) => None
    }
  }
}