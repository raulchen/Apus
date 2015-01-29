package apus.auth

import apus.protocol.Jid

import scala.concurrent.Future

/**
 * Always return true for authentication.
 * Created by Hao Chen on 2014/11/30.
 */
object AnonymousUserAuth extends UserAuth{

  override def auth(jid: Jid, password: String): Future[Boolean] = Future.successful(true)
}
