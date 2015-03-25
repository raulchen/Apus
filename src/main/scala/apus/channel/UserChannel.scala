package apus.channel

import apus.cluster.{CheckUserChannelRelocation, NewNodeUp}
import apus.dao.SavedUserMessage
import apus.protocol.Message
import apus.server.ServerRuntime

import scala.concurrent.duration._

import akka.actor.Actor.Receive
import akka.actor._

import scala.util.{Success, Failure}

/*
 * Created by Hao Chen on 2014/11/26.
 */

/**
 * A channel actor for handling a user's incoming messages
 */
class UserChannel(userId: String, runtime: ServerRuntime) extends Actor with ActorLogging{

  import context.dispatcher

  var sessions = Set.empty[ActorRef]

  val idleTimeout = {
    import com.github.kxbmap.configs._
    runtime.config.get[Duration]("apus.user-channel.idle-timeout")
  }
  context.setReceiveTimeout(idleTimeout)

  override def receive: Receive = {
    case NewNodeUp =>
      runtime.router ! CheckUserChannelRelocation(userId, self)

    case RegisterSession(session, _) =>
//      println(session)
      sessions += session
      context.watch(session)
      sender ! SessionRegistered(self)

    case Terminated(session) =>
      sessions -= session

    case ReceiveTimeout =>
      if(sessions.size == 0){
        log.debug("Idle timeout, channel stopped")
        context.stop(self)
      }

    case msg: UserMessage =>
      val curSelf = self
      val f = runtime.da.messageDao.saveUserMessage(msg)
      f onComplete {
        case Success(r) => curSelf ! r
        case Failure(e) =>
          //TODO send feedback to msg.source
          log.error(e, "Failed to save UserMessage: {}", msg)
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

  def props(userId: String, runtime: ServerRuntime): Props = {
    Props(classOf[UserChannel], userId, runtime)
  }
}
