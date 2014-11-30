package apus.server

import akka.actor.{Props, ActorRef}
import akka.routing.FromConfig
import apus.auth.{AnonymousUserAuth, UserAuth}
import apus.channel.UserChannelRouter
import com.typesafe.config.Config

/**
 * A clustered XMPP server
 * Created by Hao Chen on 2014/11/30.
 */
class ClusteredXmppServer(override val config: Config) extends XmppServer{

  override val runtime: ServerRuntime = ServerRuntime.fromConfig(this, config)

  override val userAuth: UserAuth = AnonymousUserAuth

  override val router: ActorRef = {
    actorSystem.actorOf(FromConfig.props(Props[UserChannelRouter]), "localRouter")
    actorSystem.actorOf(FromConfig.props(Props[UserChannelRouter]), "router")
  }
}
