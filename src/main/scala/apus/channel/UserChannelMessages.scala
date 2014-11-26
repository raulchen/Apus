package apus.channel

import akka.actor.ActorRef
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import apus.protocol.{Jid, Message}

/**
 * Created by Hao Chen on 2014/11/26.
 */

trait UserChannelMessage extends ConsistentHashable{

  def userId: String

  override def consistentHashKey: Any = userId
}

case class RegisterSession(userJid: Jid) extends UserChannelMessage{

  def userId = userJid.node
}

case object SessionRegistered

case class ReceiveMessage(userId: String, msg: Message) extends UserChannelMessage