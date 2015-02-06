package apus.auth

import apus.protocol.Jid

import scala.concurrent.Future
import scala.util.{Failure, Success}

/*
 * Created by Hao Chen on 2015/1/28.
 */
class DefaultUserAuth(serverDomain: String) extends UserAuth{

  override def auth(jid: Jid, password: String): Future[Boolean] = ???
}
