package apus.session.handlers

import apus.channel.SessionRegistered
import apus.protocol.{ServerResponses, Iq, XmppNamespaces}
import apus.session.{SessionHandler, Session}
import apus.util.UuidGenerator

import scala.util.Success
import scala.xml.Elem

/**
 * Handle Iq stanzas.
 * Created by Hao Chen on 2014/11/24.
 */
class IqHandler(val session: Session) extends StanzaHandler[Iq] with SessionHandler{

  import apus.protocol.IqType._

  def nextResourceId() = UuidGenerator.next()

  override def handle(iq: Iq): Unit ={
    iq.typ match {
      case Set => {
        handleSet(iq)
      }
      case _ => unhandled(iq)
    }

  }

  private def handleSet(iq: Iq): Unit ={
    iq.xml.child.headOption.orNull match {
      case bind @ Elem(_, "bind", _, _, _) if bind.namespace == XmppNamespaces.BIND => {
        val resourceId = nextResourceId()
        val newJid = session.clientJid.get.copy(resourceOpt = Some(resourceId))

        registerToUserChannel(newJid, {
          case Success(SessionRegistered) => {
            reply(ServerResponses.bind(iq.id, session.clientJid.get))
          }
          case _ => {
            session.log.error(s"fail to register session for ${session.clientJid}")
          }
        })
      }
      case <session /> => {
        reply(ServerResponses.session(iq.id, session.runtime.serverJid))
      }
      case _ => unhandled(iq)
    }
  }

  private def unhandled(iq: Iq): Unit = {
    val resp = <iq xmlns="jabber:client" type="error" id={iq.id}>{iq.xml.child}<error type="cancel"><service-unavailable xmlns="urn:ietf:params:xml:ns:xmpp-stanzas"></service-unavailable></error></iq>
    reply(resp)
  }

}
