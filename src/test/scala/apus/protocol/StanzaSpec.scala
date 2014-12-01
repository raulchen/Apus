package apus.protocol

import apus.BaseSpec
import apus.util.Xml

/**
 * Test stanza parsing.
 * Created by Hao Chen on 2014/12/1.
 */
class StanzaSpec extends BaseSpec{

  "Stanza.apply" should "parse valid Iq stanza" in {
    val xml = <iq xmlns="jabber:client" type="result" id="s4gIA-1" from="cloudshare.com"></iq>
    val iq = Stanza(xml)
    iq shouldBe an[Iq]
    iq.asInstanceOf[Iq].iqType shouldBe IqType.Result
  }

  it should "parse valid Message stanza" in {
    val xml = <message to='romeo@example.net' from='juliet@example.com/balcony' type='chat' xml:lang='en'>
      <body>Wherefore art thou, Romeo?</body>
    </message>
    val msg = Stanza(xml).asInstanceOf[Message]
    msg.fromOpt.get.toString shouldBe "juliet@example.com/balcony"
    msg.to.toString shouldBe "romeo@example.net"
    msg.msgType shouldBe MessageType.Chat
    msg.body shouldBe "Wherefore art thou, Romeo?"

    //and body can be empty
    val anotherXml = Xml("""<message to="2@apus.im"><body></body></message>""")
    Stanza(anotherXml).asInstanceOf[Message].body shouldBe empty
  }

  it should "parse valid Presence stanza" in {
    val xml = <presence></presence>
    Stanza(xml) shouldBe a[Presence]
  }

  it should "parse invalid Iq stanza to UnknownStanza" in {
    val invalidIqXml = List(
      """ <iq></iq> """, // no id attribute
      """ <iq id=""></iq> """ // id empty
    )
    forAll(invalidIqXml) { xml =>
      Stanza(Xml(xml)) shouldBe an[UnknownStanza]
    }
  }

  it should "parse invalid Message stanza to UnknownStanza" in {
    val invalidMsgXml = List(
      """ <message></message> """, // no to attribute
      """ <message to="hehe"></message> """, // invalid to
      """ <message to="2@apus.im"></message>""" // no body
    )
    forAll(invalidMsgXml) { xml =>
      Stanza(Xml(xml)) shouldBe an[UnknownStanza]
    }
  }
}
