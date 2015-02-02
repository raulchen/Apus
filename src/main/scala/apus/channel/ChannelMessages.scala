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
 * A trait for actor messages that will be used for relaying xmpp message stanza
 */
trait MessageStanzaRelay{

  /**
   * @return the message stanza to relay
   */
  def stanza: Message

  /**
   * @return the source Session from which the stanza comes from
   */
  def source: ActorRef
}

/**
 * A wrapper of XMPP message that will be sent to a [[apus.channel.UserChannel]]
 * @param userId the target user's id
 * @param stanza the message stanza to relay
 * @param source the source Session from which the stanza comes from
 */
case class UserMessage(userId: String, stanza: Message, source: ActorRef)
  extends ToUserChannel with MessageStanzaRelay


/**
 * A wrapper of XMPP message that will be sent to a [[apus.channel.GroupChannel]]
 * @param stanza the message stanza to relay
 * @param source the source Session from which the stanza comes from
 */
case class GroupMessage(stanza: Message, source: ActorRef)
  extends ToGroupChannel with MessageStanzaRelay{

  override def groupId = stanza.to.node
}