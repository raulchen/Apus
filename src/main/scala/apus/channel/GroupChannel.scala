package apus.channel

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import apus.protocol.Jid

/**
 * Created by Hao Chen on 2015/1/15.
 */
class GroupChannel(groupId: String, router: ActorRef) extends Actor with ActorLogging{

  override def receive: Receive = {
    case ToGroupMessage(msg) => {
      List("1", "2", "3") foreach { userId =>
        val m = msg.copy(to = Jid.parse(userId + "@apus.im"))
        router ! ToUserMessage(userId, m)
      }
    }
  }
}

object GroupChannel {

  def props(userId: String, router: ActorRef): Props = {
    Props(classOf[GroupChannel], userId, router)
  }
}