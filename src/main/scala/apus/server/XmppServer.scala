package apus.server

import akka.actor.ActorSystem

/**
 * Created by Hao Chen on 2014/11/17.
 */
trait XmppServer {

  def startUp():Unit

  def shutDown():Unit

  def actorSystem():ActorSystem
}
