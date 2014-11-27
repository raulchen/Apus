package apus.protocol

import scala.util.control.NonFatal

/*
 * Created by Hao Chen on 2014/11/17.
 */

/**
 * The Jabber ID
 * @param nodeOpt the node part
 * @param domain the domain part
 * @param resourceOpt the resource part
 */
case class Jid(nodeOpt: Option[String],
               domain: String,
               resourceOpt: Option[String]) {

  def node: String = nodeOpt.get

  //  def resource: String = resourceOpt.getOrElse("")

  /**
   * @return the bare jid without resource part
   */
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
    sb.toString()
  }
}

object Jid {

  private val validChars = "[^@/]+"
  private val pattern = s"(($validChars)@)?($validChars)(/($validChars))?".r

  /**
   * Parse a String to Jid.
   * If the string is not well-formed, An IllegalArgumentException will be thrown.
   *
   * @param str
   * @param requireNode whether this jid must have the node part
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

  /**
   * Parse a String to Jid.
   * Return None if the string is not well-formed.
   * @param str
   * @param requireNode whether this jid must have the node part
   * @return
   */
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