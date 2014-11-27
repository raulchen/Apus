package apus.session

import scala.concurrent.duration._

import akka.pattern.ask

import apus.channel.{SessionRegistered, RegisterSession}
import apus.protocol.Jid

import scala.util.Try
import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/25.
 */
trait SessionHandler {

  def session(): Session

  /**
   * reply message to client
   * @param msg
   * @return
   */
  def reply(msg: Elem) = {
    session.ctx.writeAndFlush(msg)
  }

  /**
   * change current client jid of this session
   * @param newJid
   */
  def setClientJid(newJid: Jid) = {
    session.clientJid = Some(newJid)
  }

  /**
   * change client jid and register this session to the corresponding user channel
   * @param newJid
   * @param completeCallback
   * @tparam U
   */
  def registerToUserChannel[U](newJid: Jid, completeCallback: Try[Any] => U): Unit ={
    setClientJid(newJid)

    val router = session.config.router
    val msg = new RegisterSession(session.self, session.clientJid.get)
    val f = ask(router, msg)(3.seconds)
    f.onComplete(completeCallback)(session.context.dispatcher)
  }
}
