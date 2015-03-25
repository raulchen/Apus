package apus.cluster

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.routing.Broadcast
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import apus.channel.ToUserChannel
import apus.server.ServerRuntime

import scala.concurrent.duration.FiniteDuration

/*
 * Created by Hao Chen on 2015/3/25.
 */


class ClusterListener(runtime: ServerRuntime, localRouter: ActorRef)
  extends Actor with ActorLogging {

  val cluster = Cluster(runtime.actorSystem)

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    case MemberUp(member) => {
      if(!member.address.equals(cluster.selfAddress)){
        runtime.actorSystem.scheduler
          .scheduleOnce(FiniteDuration(3, TimeUnit.SECONDS)){
          localRouter ! Broadcast(NewNodeUp)
        }
      }
    }
  }
}

object ClusterListener{

  def props(runtime: ServerRuntime, localRouter: ActorRef): Props = {
    Props(classOf[ClusterListener], runtime, localRouter)
  }
}

case object NewNodeUp

case class CheckUserChannelRelocation(userId: String, prevUserChannel: ActorRef)
  extends ConsistentHashable{

  override def consistentHashKey: Any = userId
}