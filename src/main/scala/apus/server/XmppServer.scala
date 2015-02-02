package apus.server

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import apus.auth.UserAuth
import apus.dao._
import apus.network.TcpEndpoint
import com.typesafe.config.Config

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

  val da: DataAccess

  val actorSystem = ActorSystem("apus", config)

  val log = Logging(actorSystem, this.getClass)

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

trait DataAccess{

  def userAuth: UserAuth

  def messageDao: MessageDao

  def groupDao: GroupDao
}

