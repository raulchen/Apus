package apus.session

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.actor.Actor.Receive
import apus.protocol.{Jid, XmppNamespaces, ServerResponses, StreamStart}
import apus.server.ServerConfig
import io.netty.channel.Channel

/**
 * Created by Hao Chen on 2014/11/17.
 */
class Session(channel: Channel, config: ServerConfig) extends Actor with ActorLogging{

  import SessionState._

  val id = config.nextSessionId

  var state = INITIALIZED

  var clientJid: Option[Jid] = None

  var userChannel: Option[ActorRef] = None

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    //TODO find user channel
  }

  private def goto(newState: SessionState.Value): Unit ={
    newState match {
      case INITIALIZED => context.become(initialized)
      case STARTED => context.become(started)
      case ENCRYPTED => context.become(encrypted)
      case AUTHENTICATED => context.become(authenticated)
      case ENDED => context.become(ended)
      case _ => throw new AssertionError
    }
    state = newState
  }

  private def handleStreamStart: Receive = {
    case StreamStart => {
      channel.write(ServerResponses.streamOpenerForClient(state,config.serverJid,Some(id)))
      state match {
        case INITIALIZED => goto(STARTED)
        case ENCRYPTED =>
        case AUTHENTICATED =>
      }
    }
  }

  private def switchToTls(): Unit ={
    val pipeline = channel.pipeline()
    pipeline.addFirst(config.sslContext.newHandler(channel.alloc()))
  }

  def initialized: Receive = handleStreamStart

  def started: Receive = {
    case elem @ <starttls /> if elem.namespace == XmppNamespaces.TLS => {
      switchToTls
      goto(ENCRYPTED)
    }
  }

  def encrypted: Receive = handleStreamStart orElse {
    case elem @ <auth /> if elem.namespace == XmppNamespaces.SASL =>{
      val node = Some(ThreadLocalRandom.current().nextInt(10000).toString)
      clientJid = Some(new Jid(node, config.serverDomain))
      goto(AUTHENTICATED)
    }
  }

  def authenticated: Receive = handleStreamStart orElse {
    case iq @ <iq /> => _
    case presence @ <presence/> => _
    case msg @ <message /> => _
  }

  def ended: Receive = ???

  override def receive: Receive = initialized
}


object Session{


}

object SessionState extends Enumeration{

  val INITIALIZED, STARTED, ENCRYPTED, AUTHENTICATED, ENDED = Value
}
