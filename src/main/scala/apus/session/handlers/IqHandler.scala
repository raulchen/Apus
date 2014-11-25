package apus.handlers

import apus.protocol.{ServerResponses, Iq, XmppNamespaces}
import apus.session.{SessionHandler, Session}
import apus.util.UuidGenerator

import scala.xml.Elem

/**
 * Created by Hao Chen on 2014/11/24.
 */
class IqHandler(val session: Session) extends StanzaHandler[Iq] with SessionHandler{

  import apus.protocol.IqType._

  val resourceIdGenerator = new UuidGenerator

  override def handle(iq: Iq): Unit ={
    iq.iqType match {
      case SET => {
        handleSet(iq)
      }
      case _ => unhandled(iq)
    }

  }

  private def handleSet(iq: Iq): Unit ={
    iq.xml.child.headOption.getOrElse(null) match {
      case bind @ Elem(_, "bind", _, _, _) if bind.namespace == XmppNamespaces.BIND => {
        val resourceId = resourceIdGenerator.next
        session.clientJid = session.clientJid.map(_.copy(resource=Some(resourceId)))
        reply(ServerResponses.bind(iq.id.getOrElse(""), session.clientJid.get))
      }
      case <session /> => reply(ServerResponses.session(iq.id.getOrElse(""), session.config.serverJid))
      case _ => unhandled(iq)
    }
  }

  private def unhandled(iq: Iq): Unit = {
    val resp = <iq xmlns="jabber:client" type="error" id={iq.id.getOrElse("")}>{iq.xml.child}<error type="cancel"><service-unavailable xmlns="urn:ietf:params:xml:ns:xmpp-stanzas"></service-unavailable></error></iq>
    reply(resp)
  }

}
