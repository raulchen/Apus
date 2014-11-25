package apus.server

import akka.actor.ActorSystem
import apus.auth.UserAuth
import apus.protocol.Jid
import io.netty.handler.ssl.SslContext

/**
 * Created by Hao Chen on 2014/11/17.
 */
trait ServerConfig {

  val domain: String

  val port: Int

  val sslContext: SslContext

  val serverJid = Jid(domain)

  val nextSessionId: String

  def actorSystem(): ActorSystem

  def userAuth(): UserAuth
}
