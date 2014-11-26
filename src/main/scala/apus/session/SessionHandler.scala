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

  def reply(msg: Elem) = {
    session.ctx.writeAndFlush(msg)
  }

  def setClientJid(newJid: Jid) = {
    session.clientJid = Some(newJid)
  }

  def registerToUserChannel[U](newJid: Jid, completeCallback: Try[Any] => U): Unit ={
    setClientJid(newJid)

    val router = session.config.router
    val msg = new RegisterSession(session.clientJid.get)
    val f = ask(router, msg)(3.seconds)
    f.onComplete(completeCallback)(session.context.dispatcher)
  }
}
