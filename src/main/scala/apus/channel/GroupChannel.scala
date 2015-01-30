package apus.channel

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import apus.protocol.Jid
import apus.server.ServerRuntime

/**
 * Created by Hao Chen on 2015/1/15.
 */
class GroupChannel(groupId: String, runtime: ServerRuntime) extends Actor with ActorLogging{

  val router = runtime.router

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

  def props(userId: String, runtime: ServerRuntime): Props = {
    Props(classOf[GroupChannel], userId, runtime)
  }
}