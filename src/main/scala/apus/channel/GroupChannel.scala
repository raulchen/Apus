package apus.channel

import akka.actor.{Actor, ActorLogging, Props}
import apus.dao.{SavedGroupMessage, GroupMembers}
import apus.protocol.Jid
import apus.server.ServerRuntime

import scala.util.{Failure, Success}

/*
 * Created by Hao Chen on 2015/1/15.
 */

/**
 * A channel actor for handling a group's incoming messages
 */
class GroupChannel(groupId: String, runtime: ServerRuntime) extends Actor with ActorLogging{

  import akka.pattern._
  import context.dispatcher

  val router = runtime.router

  var members: Seq[Jid] = _

  var buffer: List[GroupMessage] = Nil

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    //fetch group members
    runtime.da.groupDao.members(groupId) pipeTo self
  }

  def initializing: Receive = {
    case GroupMembers(memberSeq) =>
      members = memberSeq
      buffer foreach relay
      buffer = Nil
      context.become(ready)
      log.debug("Group {] initialized", groupId)

    case m: GroupMessage =>
      //TODO check buffer size
      buffer = buffer :+ m
  }

  def ready: Receive = {
    case m: GroupMessage =>
      val curSelf = self
      val f = runtime.da.messageDao.saveGroupMessage(m)
      f onComplete {
        case Success(r) => curSelf ! r
        case Failure(e) =>
          //TODO send feedback to msg.source
          log.error(e, "Failed to save GroupMessage: {}", m)
      }

    case SavedGroupMessage(m) =>
      relay(m)
  }

  override def receive: Receive = initializing

  def relay(m: GroupMessage): Unit ={
    members foreach { member =>
      val copy = m.stanza.copy(to = Some(member))
      router ! UserMessage(member.node, copy, m.source)
    }
  }
}

object GroupChannel {

  def props(userId: String, runtime: ServerRuntime): Props = {
    Props(classOf[GroupChannel], userId, runtime)
  }
}