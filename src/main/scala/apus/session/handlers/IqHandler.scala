package apus.session.handlers

import apus.channel.SessionRegistered
import apus.protocol.{Iq, ServerResponses, XmppNamespaces}
import apus.session.{Session, SessionHelper}

import scala.util.{Failure, Success}
import scala.xml.Elem

/**
 * Handle Iq stanzas.
 * Created by Hao Chen on 2014/11/24.
 */
class IqHandler(val session: Session) extends StanzaHandler[Iq] with SessionHelper{

  import apus.protocol.IqType._

//  def nextResourceId() = UuidGenerator.next()
  def nextResourceId() = session.id

  override def handle(iq: Iq): Unit ={
    iq.typ match {
      case Set =>
        handleSet(iq)

      case _ =>
        unhandled(iq)
    }

  }

  private def handleSet(iq: Iq): Unit ={
    iq.xml.child.headOption.orNull match {
      case bind @ Elem(_, "bind", _, _, _) if bind.namespace == XmppNamespaces.BIND =>
        val resourceId = nextResourceId()
        val newJid = session.clientJid.get.copy(resourceOpt = Some(resourceId))

        registerToUserChannel(newJid){
          case Success(SessionRegistered(userChannel)) =>
            session.context.watch(userChannel)
            reply(ServerResponses.bind(iq.id, session.clientJid.get))

          case Failure(e) =>
            session.log.error(e, s"failed to register session for ${session.clientJid}")

          case _ =>
        }

      case <session /> =>
        reply(ServerResponses.session(iq.id, session.runtime.serverJid))

      case _ => unhandled(iq)
    }
  }

  private def unhandled(iq: Iq): Unit = {
    val resp = <iq xmlns="jabber:client" type="error" id={iq.id}>{iq.xml.child}<error type="cancel"><service-unavailable xmlns="urn:ietf:params:xml:ns:xmpp-stanzas"></service-unavailable></error></iq>
    reply(resp)
  }

}
