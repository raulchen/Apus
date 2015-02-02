package apus.channel

import apus.dao.SavedUserMessage
import apus.protocol.Message
import apus.server.ServerRuntime

import scala.concurrent.duration._

import akka.actor.Actor.Receive
import akka.actor._

import scala.util.{Success, Failure}

/**
 * Created by Hao Chen on 2014/11/26.
 */
class UserChannel(userId: String, runtime: ServerRuntime) extends Actor with ActorLogging{

  import UserChannel._
  import context.dispatcher

  var sessions = Set.empty[ActorRef]

  context.setReceiveTimeout(IdleTimeoutDuration)

  override def receive: Receive = {
    case RegisterSession(session, _) =>
//      println(session)
      sessions += session
      context.watch(session)
      sender ! SessionRegistered

    case Terminated(session) =>
      sessions -= session

    case ReceiveTimeout =>
      if(sessions.size == 0){
        log.debug("idle timeout, channel stops")
        context.stop(self)
      }

    case msg: UserMessage =>
      val curSelf = self
      val f = runtime.da.messageDao.saveUserMessage(msg)
      f onComplete {
        case Success(r) => curSelf ! r
        case Failure(e) =>
          //TODO send feedback to msg.source
      }

    case SavedUserMessage(msg) =>
      relay(msg)

  }

  private def relay(msg: UserMessage): Unit ={
    if(sessions.isEmpty){
      //TODO push notification
      log.debug("No active session for user {}", userId)
    }
    else{
      for(session <- sessions){
        session ! msg
      }
    }
  }
}

object UserChannel {

  private val IdleTimeoutDuration = 1.minutes

  def props(userId: String, runtime: ServerRuntime): Props = {
    Props(classOf[UserChannel], userId, runtime)
  }
}
