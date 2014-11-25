package apus.server

import java.util.concurrent.ThreadLocalRandom

import akka.actor.ActorSystem
import apus.auth.MockUserAuth
import apus.network.TcpEndpoint
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.util.SelfSignedCertificate

/**
 * Created by Hao Chen on 2014/11/17.
 */
trait XmppServer {

  def startUp():Unit

  def shutDown():Unit

}

object DefaultXmppServer extends XmppServer{

  val config = new ServerConfig {

    override def actorSystem(): ActorSystem = DefaultXmppServer.this.actorSystem

    override val nextSessionId: String = ThreadLocalRandom.current().nextLong.toString
    override lazy val domain: String = "apus.im"

    val ssc = new SelfSignedCertificate()
    override val sslContext: SslContext = SslContext.newServerContext(ssc.certificate, ssc.privateKey)
    override val port: Int = 5222

    override val userAuth = MockUserAuth
  }

  val actorSystem = ActorSystem("Apus")

  override def startUp(): Unit = {
    val endpoint = new TcpEndpoint(5222, config)
    endpoint.start
  }

  override def shutDown(): Unit = ???
}