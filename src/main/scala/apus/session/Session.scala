package apus.session

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import apus.handlers.IqHandler
import apus.protocol._
import apus.server.ServerConfig
import apus.session.auth.Mechanism
import io.netty.channel.ChannelHandlerContext

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/17.
 */
class Session(val ctx: ChannelHandlerContext, val config: ServerConfig) extends Actor
  with ActorLogging with SessionHandler{

  import apus.session.SessionState._

  override val session: Session = this

  val id = config.nextSessionId

  var state = INITIALIZED

  var clientJid: Option[Jid] = None

  var userChannel: Option[ActorRef] = None

  val mechanism = new Mechanism(this)

  val iqHandler = new IqHandler(this)

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    //TODO find user channel
  }

  private def become(newState: SessionState.Value): Unit ={
    log.debug("becoming {}.", newState.toString)
    newState match {
      case INITIALIZED => context.become(initialized)
      case STARTED => context.become(started)
      case ENCRYPTED => context.become(encrypted)
      case AUTHENTICATED => context.become(authenticated)
      case ENDED => context.become(ended)
      case _ => throw new AssertionError("invalid session state")
    }
    state = newState
  }

  private def handleStreamStart: Receive = {
    case StreamStart => {
      reply(ServerResponses.streamOpenerForClient(state,config.serverJid,Some(id)))
      state match {
        case INITIALIZED => become(STARTED)
        case ENCRYPTED =>
        case AUTHENTICATED =>
      }
    }
  }

  private def switchToTls(): Unit = {
    reply(ServerResponses.tlsProceed)
    val handler = config.sslContext.newHandler(ctx.channel.alloc())
    ctx.channel.pipeline.addFirst("sslHandler", handler)
  }

  private def connectToUserChannel(jid: Jid): Unit = {
    println(jid)
  }

  def initialized: Receive = handleStreamStart

  def started: Receive = {
    case elem @ <starttls /> if elem.namespace == XmppNamespaces.TLS => {
      switchToTls
      become(ENCRYPTED)
    }
  }

  def encrypted: Receive = handleStreamStart orElse {
    case elem @ Elem(_, "auth", _, _, child) if elem.namespace == XmppNamespaces.SASL =>{
      if(mechanism.auth(child.text)){
        become(AUTHENTICATED)
      }
    }
  }

  def authenticated: Receive = handleStreamStart orElse {
    case elem: Elem => {
      val stanza = Stanza(elem)
      stanza match {
        case iq: Iq => iqHandler.handle(iq)
        case presence: Presence => println(presence)
        case msg: Message => println(msg)
      }
    }
  }

  def ended: Receive = ???

  override def receive: Receive = initialized
}

object Session {

  def props(ctx: ChannelHandlerContext, config: ServerConfig): Props = {
    Props(classOf[Session], ctx, config)
  }
}

object SessionState extends Enumeration{

  val INITIALIZED, STARTED, ENCRYPTED, AUTHENTICATED, ENDED = Value
}
