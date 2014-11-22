package apus.server

import akka.actor.ActorSystem
import apus.protocol.Jid

/**
 * Created by Hao Chen on 2014/11/17.
 */
trait XmppServer {

  this: ServerConfig => _

  val serverJid = Jid(serverDomain)

  def startUp():Unit

  def shutDown():Unit


}
