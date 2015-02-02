package apus.server

import akka.actor.{Props, ActorRef}
import akka.routing.FromConfig
import apus.auth.{AnonymousUserAuth, UserAuth}
import apus.channel.ChannelRouter
import com.typesafe.config.Config

/**
 * A stand-alone XMPP server
 * Created by Hao Chen on 2014/11/30.
 */
class StandAloneXmppServer(override val config: Config) extends XmppServer{

  override val runtime: ServerRuntime = ServerRuntime.fromConfig(this, config)

  override val da = MockDataAccess

  override val router: ActorRef = {
    actorSystem.actorOf(FromConfig.props(ChannelRouter.props(runtime)), "router")
  }
}
