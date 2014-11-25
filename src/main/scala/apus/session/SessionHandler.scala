package apus.session

import apus.protocol.Jid

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/25.
 */
trait SessionHandler {

  def session(): Session

  def reply(msg: Elem) = {
    session.ctx.writeAndFlush(msg)
  }

  def setJid(jid: Jid) = {
    session.clientJid = Some(jid)
  }
}
