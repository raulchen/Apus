package apus.auth

import apus.protocol.Jid

/**
 * Created by Hao Chen on 2014/11/25.
 */
trait UserAuth {

  def verify(jid: Jid, password: String): Boolean
}

object MockUserAuth extends UserAuth{

  override def verify(jid: Jid, password: String): Boolean = true
}
