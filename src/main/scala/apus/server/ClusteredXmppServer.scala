package apus.server

import akka.actor.{Props, ActorRef}
import akka.routing.ConsistentHashingRouter.{ConsistentHashable, ConsistentHashMapping}
import akka.routing.{ConsistentHashingPool, FromConfig}
import apus.auth.{AnonymousUserAuth, UserAuth}
import apus.channel.ChannelRouter
import com.typesafe.config.Config

/**
 * A clustered XMPP server
 * Created by Hao Chen on 2014/11/30.
 */
class ClusteredXmppServer(override val config: Config) extends XmppServer{

  override val runtime: ServerRuntime = ServerRuntime.fromConfig(this, config)

  override val da = MockDataAccess

  override val router: ActorRef = {

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
//    actorSystem.actorOf(Props[UserChannelRouter], "localRouter")
    actorSystem.actorOf(FromConfig.props(), "router")
  }
}
