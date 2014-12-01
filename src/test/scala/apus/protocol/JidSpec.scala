package apus.protocol

import apus.BaseSpec
import org.scalatest.{Matchers, FlatSpec}

import scala.collection.mutable

/**
 * Test Jid
 * Created by Hao Chen on 2014/12/1.
 */
class JidSpec extends BaseSpec{

  val inValidStrings = List(
    "", "apus.im", "@apus.im", "apus.im/",
    "a@apus.im/", "@apus.im/b",
    "a@a@apus.im", "apus.im/b/b"
  )

  "Jid.apply" should "parses all these strings as valid Jid" in {
    Jid("apus.im", requireNode = false) shouldBe new Jid(None, "apus.im", None)
    Jid("a@apus.im") shouldBe new Jid(Some("a"), "apus.im", None)
    Jid("a@apus.im/.b") shouldBe new Jid(Some("a"), "apus.im", Some(".b"))
  }

  "Jid.apply" should "throws exception when accepting these inputs" in {
    inValidStrings.foreach{ str =>
      a [IllegalArgumentException] should be thrownBy {
        Jid(str)
      }
    }
  }

  "Jid.parse" should "returns None if handling invalid strings" in {
    inValidStrings foreach { str =>
      Jid.parse(str) should be (scala.None)
    }
  }

  "Jid.toString" should "ignores empty node/resource part" in {
    Jid("apus.im", requireNode = false).toString shouldBe "apus.im"
    Jid("apus.im/b", requireNode = false).toString shouldBe "apus.im/b"
    Jid("a@apus.im").toString shouldBe "a@apus.im"
    Jid("a@apus.im/b").toString shouldBe "a@apus.im/b"
  }
}
