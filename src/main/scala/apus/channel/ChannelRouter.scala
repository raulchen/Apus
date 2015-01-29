package apus.channel

import akka.actor._
import akka.actor.Actor.Receive
import apus.server.ServerRuntime
import apus.util.BiMap

/**
 * Created by Hao Chen on 2014/11/26.
 */
class ChannelRouter(val runtime: ServerRuntime) extends Actor{

  val router = runtime.router()

  private def findUserChannel(userId: String): ActorRef = {
    val name = "u-" + userId
    context.child(name) match {
      case Some(child) => child
      case None => context.actorOf(UserChannel.props(userId), name)
    }
  }

  private def findGroupChannel(groupId: String): ActorRef = {
    val name = "g-" + groupId
    context.child(name) match {
      case Some(child) => child
      case None => context.actorOf(GroupChannel.props(groupId, router), name)
    }
  }

  override def receive: Receive = {
    case x: ToUserChannel =>
      findUserChannel(x.userId).forward(x)

    case x: ToGroupChannel =>
      findGroupChannel(x.groupId).forward(x)
  }
}

object ChannelRouter{

  def props(runtime: ServerRuntime): Props = {
    Props(classOf[ChannelRouter], runtime)
  }
}