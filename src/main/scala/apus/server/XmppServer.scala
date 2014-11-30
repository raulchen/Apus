package apus.server

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.event.Logging
import akka.routing.{FromConfig, ConsistentHashingPool}
import apus.auth.{UserAuth, AnonymousUserAuth}
import apus.channel.UserChannelRouter
import apus.network.TcpEndpoint
import com.typesafe.config.Config
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.util.SelfSignedCertificate

/*
 * Created by Hao Chen on 2014/11/17.
 */


/**
 * The XMPP Server
 */
abstract class XmppServer{

  val config: Config

  val runtime: ServerRuntime

  val router: ActorRef

  val userAuth: UserAuth

  val actorSystem = ActorSystem("apus", config)

  val log = Logging(actorSystem.eventStream, this.getClass.getCanonicalName)

  lazy val endPoints = List(new TcpEndpoint(runtime.port, runtime))

  def startUp(): Unit = {
    endPoints.foreach( _.start() )
    log.info("Server started on {}", runtime.port)
  }

  def shutDown(): Unit = {
    endPoints.foreach( _.shutdown() )
    actorSystem.shutdown()
    log.info("Server stopped")
  }

}