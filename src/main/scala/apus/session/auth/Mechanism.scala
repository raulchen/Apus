package apus.session.auth

import java.util.Base64

import apus.protocol.{ServerResponses, Jid}
import apus.session.{SessionHandler, Session}

/**
 * Created by Hao Chen on 2014/11/25.
 */
class Mechanism(val session: Session) extends SessionHandler{

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

  def auth(encoded: String): Boolean = {
    val decoded = Base64.getDecoder.decode(encoded)
    val parts = split(decoded)
    if(parts.length != 3){
      reply(ServerResponses.authFailureMalformedRequest)
      return false
    }
    var (alias, username, password) = (parts(0), parts(1), parts(2))

    val config = session.config
    if(!username.contains("@")){
      username = username + "@" + config.domain
    }

    val jid = Jid.parse(username)
    if(jid.isDefined) {
      setClientJid(jid.get)
      if (config.userAuth.verify(jid.get, password)) {
        reply(ServerResponses.authSuccess)
        return true
      }
      else {
        return false
      }
    }
    else{
      reply(ServerResponses.authFailureMalformedRequest)
      return false
    }
  }
}
