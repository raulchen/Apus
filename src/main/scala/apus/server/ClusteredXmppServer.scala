package apus.server

import akka.actor.{Address, ActorLogging, Actor, ActorRef}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberUp, InitialStateAsEvents}
import akka.routing.ConsistentHashingRouter.{ConsistentHashMapping, ConsistentHashable}
import akka.routing.{Broadcast, ConsistentHashingPool, FromConfig}
import apus.channel.ChannelRouter
import apus.cluster.ClusterListener
import com.typesafe.config.Config

/**
 * A clustered XMPP server
 * Created by Hao Chen on 2014/11/30.
 */
class ClusteredXmppServer(override val config: Config) extends XmppServer{

  override val runtime: ServerRuntime = ServerRuntime.fromConfig(this, config)

  override val da = MockDataAccess

  val localRouter: ActorRef = {
    val localRouterProps = {
      //we need to re-calculate hashing in a different way in the local router
      //to avoid hash code bias
      import com.github.kxbmap.configs._
      val magic = 0x971e137b
      val localRouterRehashing: ConsistentHashMapping = {
        case x: ConsistentHashable => (x.consistentHashKey, magic)
      }
      val nrOfInstances = config.get[Int]("akka.actor.deployment./localRouter.nr-of-instances")
      val virtualNodesFactor = config.opt[Int]("akka.actor.deployment./localRouter.virtual-nodes-factor").getOrElse(10)
      ConsistentHashingPool(nrOfInstances,
        virtualNodesFactor = virtualNodesFactor,
        hashMapping = localRouterRehashing).props(ChannelRouter.props(runtime))
    }

    actorSystem.actorOf(localRouterProps, "localRouter")
  }

  override val router: ActorRef = actorSystem.actorOf(FromConfig.props(), "router")

  val clusterListener = actorSystem.actorOf(ClusterListener.props(runtime, localRouter))
}


