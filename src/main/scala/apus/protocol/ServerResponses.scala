package apus.protocol

import apus.session.SessionState
import apus.session.SessionState

import scala.xml.{NodeSeq, Elem, Node}

/**
 * Created by Hao Chen on 2014/11/18.
 */
object ServerResponses {

  import apus.util.XmlImplicit._
  import XmppNamespaces._

  //============== FEATURES ==============

  private def features(innerNodes: NodeSeq): Elem = {
    <stream:features xmlns:stream={STREAM}>
      { innerNodes }
    </stream:features>
  }

  def featuresForEncryption(): Elem = {
    features(
      <starttls xmlns={TLS}><required></required></starttls>
    )
  }

  def featuresForAuthentication(): Elem = {
    features(
      <mechanisms xmlns={SASL}><mechanism>{SaslMechanism.Plain}</mechanism></mechanisms>
    )
  }

  def featuresForSession(): Elem = {
    features(
      <bind xmlns={BIND}><required></required></bind>
      <session xmlns={SESSION}><required></required></session>
    )
  }

  //============

  def tlsProceed(): Elem = {
    <proceed xmlns={TLS}></proceed>
  }

  def authAborted(): Elem = {
    <aborted xmlns={TLS}></aborted>
  }

  def streamOpenerForClient(state: SessionState.Value, from: Jid, sessionId: Option[String]): Elem ={
    val innerNodes = state match{
      case SessionState.INITIALIZED => featuresForEncryption()
      case SessionState.ENCRYPTED => featuresForAuthentication()
      case SessionState.AUTHENTICATED => featuresForSession() //TODO sessionContext.setIsReopeningXMLStream();
    }

    <stream:stream xmlns={CLIENT} xmlns:stream={STREAM} from={from} version="1.0" id={sessionId}>{innerNodes}</stream:stream>
  }
}
