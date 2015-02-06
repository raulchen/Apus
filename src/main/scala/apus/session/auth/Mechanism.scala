package apus.session.auth

import java.util.Base64

import apus.protocol.Jid

/**
* Created by Hao Chen on 2014/11/25.
*/
object Mechanism {

  private def split(b: Array[Byte]) = {
    var res: List[String] = Nil
    var last = 0
    for( i <- 0 to b.length ){
      if( i==b.length || b(i) == 0 ){
        res = res :+ b.slice(last, i).map(_.asInstanceOf[Char]).mkString
        last = i+1
      }
    }
    res
  }

  /**
   * decode encoded auth info to (jid, password) pair
   * @param encoded encoded auth info
   * @return Option of (jid, password) pair, None if the encoded string is malformed
   */
  def decode(encoded: String, domain: String): Option[(Jid, String)] = {
    val decoded = Base64.getDecoder.decode(encoded)
    val parts = split(decoded)
    if (parts.length != 3) {
      None
    }
    else {
      var (alias, username, password) = (parts(0), parts(1), parts(2))

      if (!username.contains("@")) {
        username = username + "@" + domain
      }

      Jid.parse(username).map((_, password))
    }
  }
}
