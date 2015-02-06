package apus.session

import akka.pattern.ask
import apus.channel.RegisterSession
import apus.protocol.Jid
import io.netty.channel.{ChannelFuture, ChannelFutureListener}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Try
import scala.xml.Elem

/**
 * the trait for some helper classes for Session
 * Created by Hao Chen on 2014/11/25.
 */
trait SessionHelper {

  def session: Session

  /**
   * reply message to client
   * @param msg msg for response
   * @return
   */
  def reply(msg: Elem): Future[Unit] = {
    val cf = session.ctx.writeAndFlush(msg)
    toScalaFuture(cf)
  }

  private def toScalaFuture(cf: ChannelFuture): Future[Unit] = {
    val promise = Promise[Unit]()

    cf.addListener(new ChannelFutureListener {

      override def operationComplete(future: ChannelFuture): Unit = {
        if(future.isSuccess){
          promise.success(Unit)
        }
        else{
          promise.failure(future.cause())
        }
      }
    })

    promise.future
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
  def registerToUserChannel[U](newJid: Jid)(completeCallback: Try[Any] => U): Unit ={
    setClientJid(newJid)

    val router = session.runtime.router
    val msg = new RegisterSession(session.self, session.clientJid.get.node)
    val f = ask(router, msg)(3.seconds)
    f.onComplete(completeCallback)(session.context.dispatcher)
  }
}
