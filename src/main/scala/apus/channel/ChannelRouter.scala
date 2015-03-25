package apus.channel

import akka.actor._
import akka.actor.Actor.Receive
import akka.cluster.Cluster
import apus.cluster.{CheckUserChannelRelocation, NewNodeUp}
import apus.server.ServerRuntime
import apus.util.BiMap

/*
 * Created by Hao Chen on 2014/11/26.
 */

/*
 * Route messages to corresponding UserChannel/GroupChannel
 */
class ChannelRouter(runtime: ServerRuntime) extends Actor{

  val router = runtime.router

  private def findUserChannel(userId: String): ActorRef = {
    val name = "u-" + userId
    context.child(name) match {
      case Some(child) => child
      case None => context.actorOf(UserChannel.props(userId, runtime), name)
    }
  }

  private def findGroupChannel(groupId: String): ActorRef = {
    val name = "g-" + groupId
    context.child(name) match {
      case Some(child) => child
      case None => context.actorOf(GroupChannel.props(groupId, runtime), name)
    }
  }

  override def receive: Receive = {
    case x: ToUserChannel =>
      findUserChannel(x.userId).forward(x)

    case x: ToGroupChannel =>
      findGroupChannel(x.groupId).forward(x)

    case NewNodeUp =>
      context.actorSelection("u-*").forward(NewNodeUp)

    case CheckUserChannelRelocation(_, prevUserChannel) =>
      val address = prevUserChannel.path.address
      println(address)
      if (address.hasGlobalScope) {
        //if it's a remote address
        prevUserChannel ! PoisonPill
      }
  }
}

object ChannelRouter{

  def props(runtime: ServerRuntime): Props = {
    Props(classOf[ChannelRouter], runtime)
  }
}