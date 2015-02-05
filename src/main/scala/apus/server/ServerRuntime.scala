package apus.server

import java.util.concurrent.atomic.AtomicLong

import akka.actor.{ActorRef, ActorSystem}
import apus.auth.UserAuth
import apus.dao.GroupDao
import apus.protocol.Jid
import apus.session.Session
import apus.util.UuidGenerator
import com.typesafe.config.Config
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.util.SelfSignedCertificate

/**
 * Runtime context of current XMPP server.
 * Created by Hao Chen on 2014/11/17.
 */
trait ServerRuntime {

  val domain: String
  lazy val serverJid = Jid(domain, requireNode = false)

  val port: Int

  val sslContext: SslContext

  def actorSystem: ActorSystem

  def config = actorSystem.settings.config

  def createSession(ctx: ChannelHandlerContext): ActorRef

  def router: ActorRef

  def da: DataAccess
}



object ServerRuntime{

  /**
   * construct a ServerRuntime instance from the config
   * @param server
   * @param config
   * @return
   */
  def fromConfig(server: XmppServer, config: Config) = {
    new ConfiguredServerRuntime(server, config)
  }
}

class ConfiguredServerRuntime(server: XmppServer, config: Config) extends ServerRuntime{

  override val domain: String = config.getString("apus.server.domain")

  override val port: Int = config.getInt("apus.server.port")

  private val ssc = new SelfSignedCertificate()
  override val sslContext: SslContext = SslContext.newServerContext(ssc.certificate, ssc.privateKey)

  override def actorSystem: ActorSystem = server.actorSystem

  private val sessionId = new AtomicLong(0)
  override def createSession(ctx: ChannelHandlerContext): ActorRef = {
    val id = sessionId.incrementAndGet().toString
    actorSystem.actorOf(Session.props(id, this, ctx), name = s"s-$id")
  }

  override def router: ActorRef = server.router

  override def da: DataAccess = server.da

}