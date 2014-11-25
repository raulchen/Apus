package apus.protocol

import apus.session.SessionState

import scala.xml._

/**
 * Created by Hao Chen on 2014/11/18.
 */
object ServerResponses {

  import apus.util.XmlImplicits._
  import apus.protocol.XmppNamespaces._

  //========= Stream Opener ==========

  def streamOpenerForClient(state: SessionState.Value, from: Jid, sessionId: Option[String]): Elem ={
    val innerNodes = state match{
      case SessionState.INITIALIZED => featuresForEncryption()
      case SessionState.ENCRYPTED => featuresForAuthentication()
      case SessionState.AUTHENTICATED => featuresForSession() //TODO sessionContext.setIsReopeningXMLStream();
    }

    <stream:stream xmlns={CLIENT} xmlns:stream={STREAM} from={from.toString} version="1.0" id={sessionId}>{innerNodes}</stream:stream>
  }

  //============== FEATURES ==============

  private def features(innerNodes: NodeSeq): Elem = {
    <stream:features xmlns:stream={STREAM}>
      { innerNodes }
    </stream:features>
  }

  def featuresForEncryption(): Elem = {
    features(
      <starttls xmlns={TLS}><required/></starttls>
    )
  }

  def featuresForAuthentication(): Elem = {
    features(
      <mechanisms xmlns={SASL}><mechanism>{SaslMechanism.Plain}</mechanism></mechanisms>
    )
  }

  def featuresForSession(): Elem = {
    features(
      <bind xmlns={BIND}><required/></bind>
      <session xmlns={SESSION}><required/></session>
    )
  }

  //============ TLS ================

  def tlsProceed(): Elem = {
    <proceed xmlns={TLS}></proceed>
  }

  //============ Auth =================

  def authSuccess(): Elem = {
    <success xmlns={SASL}></success>
  }

  def authFailureNotAuthorized(): Elem = {
    <failure xmlns={SASL}><not-authorized xmlns={SASL}/></failure>
  }

  def authFailureMalformedRequest(): Elem = {
    <failure xmlns={SASL}><malformed-request xmlns={SASL}/></failure>
  }

  //============ IQ ====================

  def bind(id: String, jid: Jid): Elem = {
    <iq xmlns="jabber:client" type="result" id={id}>
      <bind xmlns={XmppNamespaces.BIND}><jid>{jid}</jid></bind>
    </iq>
  }

  def session(id: String, from: Jid): Elem = {
    <iq xmlns="jabber:client" type="result" id={id} from={from.toString}></iq>
  }
}
