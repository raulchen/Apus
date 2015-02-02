package apus.channel

import akka.actor.{Actor, ActorLogging, Props}
import apus.dao.GroupMembers
import apus.protocol.Jid
import apus.server.ServerRuntime

/**
 * Created by Hao Chen on 2015/1/15.
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

    case m: GroupMessage =>
      //TODO check buffer size
      buffer = buffer :+ m
  }

  def ready: Receive = {
    case m: GroupMessage =>
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