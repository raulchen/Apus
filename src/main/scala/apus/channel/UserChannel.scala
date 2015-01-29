package apus.channel

import apus.protocol.Message

import scala.concurrent.duration._

import akka.actor.Actor.Receive
import akka.actor._

/**
 * Created by Hao Chen on 2014/11/26.
 */
class UserChannel(userId: String) extends Actor with ActorLogging{

  var sessions = Set.empty[ActorRef]

  override def receive: Receive = {
    case RegisterSession(session, _) => {
//      println(session)
      sessions += session
      context.watch(session)
      sender ! SessionRegistered
    }
    case Terminated(session) => {
      sessions -= session
      if(sessions.size == 0){
        //TODO wait a moment
        context.stop(self)
      }
    }
    case msg: ToUserMessage => {
      handleReceivedMessage(msg)
    }
  }

  private def handleReceivedMessage(msg: ToUserMessage): Unit ={
    //TODO save msg
    if(sessions.isEmpty){
      //TODO push notification
      log.info(s"No active session for {}", userId)
    }
    else{
      for(session <- sessions){
        session ! msg
      }
    }
  }
}

object UserChannel {

  def props(userId: String): Props = {
    Props(classOf[UserChannel], userId)
  }
}
