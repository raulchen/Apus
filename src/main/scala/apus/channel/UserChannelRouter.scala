package apus.channel

import akka.actor.{SupervisorStrategy, Terminated, ActorRef, Actor}
import akka.actor.Actor.Receive
import apus.util.BiMap

/**
 * Created by Hao Chen on 2014/11/26.
 */
class UserChannelRouter extends Actor{ //TODO make it a real Router

  private def findUserChannel(userId: String): ActorRef = {
    val c = context.child(userId)
    if(c.isDefined){
      c.get
    }
    else{
      context.actorOf(UserChannel.props(userId), userId)
    }
  }

  override def receive: Receive = {
    case m: UserChannelMessage => {
      findUserChannel(m.userId).forward(m)
    }
  }
}
