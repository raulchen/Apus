package apus.session

import akka.actor._
import apus.auth.UserAuthException
import apus.channel.{SessionRegistered, GroupMessage, UserMessage}
import apus.protocol._
import apus.server.ServerRuntime
import apus.session.Session.UserAuthResult
import apus.session.auth.Mechanism
import apus.session.handlers.IqHandler
import io.netty.channel.ChannelHandlerContext

import scala.util.{Failure, Success, Try}
import scala.xml.Elem

/**
 * The session actor.
 * Created by Hao Chen on 2014/11/17.
 */
class Session(val id: String, val runtime: ServerRuntime, val ctx: ChannelHandlerContext)
  extends Actor with ActorLogging with SessionHelper{

  import apus.session.SessionState._
  import context.dispatcher

  override val session: Session = this

  var state = INITIALIZED

  val router = runtime.router

  var clientJid: Option[Jid] = None

  val iqHandler = new IqHandler(this)

//  @scala.throws[Exception](classOf[Exception])
//  override def preStart(): Unit = {
//    super.preStart()
//  }

  /**
   * become a new state
   * @param newState
   */
  private def become(newState: SessionState.Value): Unit ={
    log.debug("session {} is becoming {}", id, newState.toString)
    newState match {
      case INITIALIZED => context.become(initialized)
      case STARTED => context.become(started)
      case ENCRYPTED => context.become(encrypted)
      case AUTHENTICATED => context.become(authenticated)
//      case ENDED => context.become(ended)
      case _ => throw new AssertionError(s"invalid session state: $newState")
    }
    state = newState
  }

  private def handleStreamStart: Receive = {
    case StreamStart => {
      reply(ServerResponses.streamOpenerForClient(state, runtime.serverJid, Some(id)))
      state match {
        case INITIALIZED => become(STARTED)
//        case INITIALIZED => become(ENCRYPTED)
        case ENCRYPTED =>
        case AUTHENTICATED =>
      }
    }
  }

  private def handleStartTls: Receive = {
    case elem @ <starttls /> if elem.namespace == XmppNamespaces.TLS => {

      def switchToTls(): Unit = {
        //make this channel encrypted
        val handler = runtime.sslContext.newHandler(ctx.channel.alloc())
        ctx.channel.pipeline.addFirst("sslHandler", handler)
      }

      val f = reply(ServerResponses.tlsProceed)
      f onSuccess {
        case _ =>
          switchToTls()
          become(ENCRYPTED)
      }
    }
  }

  private def handleAuth: Receive = {
    case elem @ Elem(_, "auth", _, _, child) if elem.namespace == XmppNamespaces.SASL => {
      Mechanism.decode(child.text, runtime.domain) match {
        case Some((jid, password)) =>
          setClientJid(jid)
          val f = runtime.da.userAuth.auth(jid, password)
          val curSelf = self
          //forward auth result to self
          f onComplete {
            t => curSelf.tell(UserAuthResult(t), curSelf)
          }

        case None =>
          reply(ServerResponses.authFailureMalformedRequest)
      }
    }
    case UserAuthResult(res) => {
      res match {
        case Success(true) =>
          reply(ServerResponses.authSuccess)
          become(AUTHENTICATED)

        case Success(false) =>
          //TODO reply

        case Failure(e) if e.isInstanceOf[UserAuthException] =>
          e.asInstanceOf[UserAuthException].resp.foreach {
            reply
          }

        case Failure(e) =>
          log.error(e, "unknown error when authenticating user")
      }
    }
  }

  def initialized: Receive = handleStreamStart

  def started: Receive = handleStartTls orElse handleAuth

  def encrypted: Receive = handleStreamStart orElse handleAuth

  def authenticated: Receive = handleStreamStart orElse {
    case elem: Elem => {
      val stanza = Stanza(elem)
      stanza match {
        case iq: Iq => iqHandler.handle(iq)
        case presence: Presence => //ignore Presence stanza for now
        case msg: Message => relayMessage(msg)
        case _ => log.warning("receive invalid stanza from [{}]: {}", clientJid, elem)
      }
    }
    case UserMessage(_, msg, _) => {
      reply(msg.xml)
    }
    case Terminated(userChannelRef) => {
      registerToUserChannel(clientJid.get){
        case Success(SessionRegistered(userChannel)) =>
          session.context.watch(userChannel)

        case Failure(e) =>
          session.log.error(e, s"failed to register session for ${session.clientJid}")

        case _ =>
      }
    }
  }

  def relayMessage(msg: Message): Unit ={
    import apus.protocol.MessageType._
    msg.typ match {
      case Chat | Normal => {
        var m = msg
        if(m.fromOpt.isEmpty){
          m = m.copy(from = clientJid)
        }
        router ! new UserMessage(msg.to.node, msg, self)
      }
      case GroupChat => {
        // from = ${groupId}@chat.apus.im/${fromId}
        val from = msg.to.copy(resourceOpt = Some(clientJid.map(_.node).get))
        val m = msg.copy(from = Some(from))
        router ! new GroupMessage(m, self)
      }
      case typ: MessageType.Value => {
        log.warning("unrecognized message type {}", typ)
      }
    }
  }

  override def receive: Receive = initialized

}

object Session {

  def props(id: String, runtime: ServerRuntime, ctx: ChannelHandlerContext): Props = {
    Props(classOf[Session], id, runtime, ctx)
  }

  //actor messages
  private case class UserAuthResult(res: Try[Boolean])
}

object SessionState extends Enumeration{

  val INITIALIZED, STARTED, ENCRYPTED, AUTHENTICATED = Value
}
