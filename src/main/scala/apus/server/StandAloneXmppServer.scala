package apus.server

import akka.actor.{Props, ActorRef}
import akka.routing.FromConfig
import apus.auth.{AnonymousUserAuth, UserAuth}
import apus.channel.ChannelRouter
import apus.dao.{MessageDaoImpl, MessageDao, GroupDao}
import com.typesafe.config.Config

/**
 * A stand-alone XMPP server
 * Created by Hao Chen on 2014/11/30.
 */
class StandAloneXmppServer(override val config: Config) extends XmppServer{

  override val runtime: ServerRuntime = ServerRuntime.fromConfig(this, config)

//  override val da = new DataAccess {
//
//    override val messageDao: MessageDao = new MessageDaoImpl(runtime)
//
//    override val groupDao: GroupDao = MockGroupDao
//
//    override val userAuth: UserAuth = AnonymousUserAuth
//  }

  override val da = MockDataAccess

  override val router: ActorRef = {
    actorSystem.actorOf(FromConfig.props(ChannelRouter.props(runtime)), "router")
  }
}
