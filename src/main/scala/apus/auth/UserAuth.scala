package apus.auth

import apus.protocol.Jid

/**
 * User authentication
 * Created by Hao Chen on 2014/11/25.
 */
trait UserAuth {

  /**
   * verify jid and password
   * @param jid
   * @param password
   * @return
   */
  def verify(jid: Jid, password: String): Boolean
}


