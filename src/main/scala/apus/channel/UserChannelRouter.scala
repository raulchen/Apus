package apus.channel

import akka.actor.{Terminated, ActorRef, Actor}
import akka.actor.Actor.Receive
import apus.util.BiMap

/**
 * Created by Hao Chen on 2014/11/26.
 */
class UserChannelRouter extends Actor{

  var channelMap = new BiMap[String, ActorRef]

  private def findUserChannel(userId: String): ActorRef = {
    var ref = channelMap.getValue(userId)
    if(ref.isDefined == false){
      val channel = context.actorOf(UserChannel.props(userId), userId)
      context.watch(channel)
      channelMap.put(userId, channel)
      ref = Some(channel)
    }
    ref.get
  }

  override def receive: Receive = {
    case m: UserChannelMessage => {
      findUserChannel(m.userId).forward(m)
    }
    case Terminated(ref) => {
      channelMap.removeByValue(ref)
    }
  }
}
