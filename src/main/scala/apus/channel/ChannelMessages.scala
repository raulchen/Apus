package apus.channel

import akka.actor.ActorRef
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import apus.protocol.{Jid, Message}

/*
 * Created by Hao Chen on 2014/11/26.
 */

/**
 * An internal message that will be sent to a [[apus.channel.UserChannel]]
 */
trait ToUserChannel extends ConsistentHashable{

  def userId: String

  override def consistentHashKey: Any = userId
}

/**
 * An internal message that will be sent to a [[apus.channel.GroupChannel]]
 */
trait ToGroupChannel extends ConsistentHashable{

  def groupId: String

  override def consistentHashKey: Any = groupId
}

/**
 * Register [[apus.session.Session]] to the [[apus.channel.UserChannel]]
 */
case class RegisterSession(session: ActorRef, userId: String) extends ToUserChannel

case object SessionRegistered

/**
 * A wrapper of XMPP message that will be sent to a [[apus.channel.UserChannel]]
 * @param userId
 * @param msg
 */
case class ToUserMessage(userId: String, msg: Message) extends ToUserChannel


/**
 * A wrapper of XMPP message that will be sent to a [[apus.channel.GroupChannel]]
 * @param msg
 */
case class ToGroupMessage(msg: Message) extends ToGroupChannel{

  override def groupId = msg.to.node
}