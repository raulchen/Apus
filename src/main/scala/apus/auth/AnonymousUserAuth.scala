package apus.auth

import apus.protocol.Jid

/**
 * Always return true for authentication.
 * Created by Hao Chen on 2014/11/30.
 */
object AnonymousUserAuth extends UserAuth{

  override def verify(jid: Jid, password: String): Boolean = true
}
