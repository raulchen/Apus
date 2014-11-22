package apus.server

import akka.actor.ActorSystem
import apus.protocol.Jid
import io.netty.handler.ssl.SslContext

/**
 * Created by Hao Chen on 2014/11/17.
 */
trait ServerConfig {

  val serverDomain: String

  val port: Int

  val sslContext: SslContext

  val serverJid = Jid(serverDomain)

  val nextSessionId: String

  def actorSystem(): ActorSystem
}
