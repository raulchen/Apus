package apus.session

import java.util.concurrent.ThreadLocalRandom

import scala.concurrent.duration._

import akka.actor._
import akka.pattern.{ask, pipe}

import apus.channel.{ReceiveMessage, SessionRegistered, RegisterSession}
import apus.session.handlers.IqHandler
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

  val router = config.router

  var clientJid: Option[Jid] = None

  val mechanism = new Mechanism(this)

  val iqHandler = new IqHandler(this)

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
  }

  private def become(newState: SessionState.Value): Unit ={
    log.debug("becoming {}.", newState.toString)
    newState match {
      case INITIALIZED => context.become(initialized)
      case STARTED => context.become(started)
      case ENCRYPTED => context.become(encrypted)
      case AUTHENTICATED => context.become(authenticated)
      case ENDED => context.become(ended)
      case _ => throw new AssertionError(s"invalid session state: $newState")
    }
    state = newState
  }

  private def handleStreamStart: Receive = {
    case StreamStart => {
      reply(ServerResponses.streamOpenerForClient(state, config.serverJid, Some(id)))
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
        case msg: Message => {
          router ! new ReceiveMessage(clientJid.get.node, msg)
        }
        case _ => log.warning("invalid elem: {}", elem)
      }
    }
    case ReceiveMessage(_, msg) => {
      reply(msg.xml)
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
