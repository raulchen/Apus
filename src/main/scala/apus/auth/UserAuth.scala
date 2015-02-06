package apus.auth

import apus.protocol.Jid

import scala.concurrent.Future
import scala.xml.Elem

/*
 * Created by Hao Chen on 2014/11/25.
 */

/**
 * User authentication
 */
trait UserAuth {

  def auth(jid: Jid, password: String): Future[Boolean]
}

class UserAuthException(val resp: Option[Elem], message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
