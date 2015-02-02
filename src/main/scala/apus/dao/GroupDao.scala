package apus.dao

import apus.protocol.Jid

import scala.concurrent.{ExecutionContext, Future}

/*
 * Created by Hao Chen on 2015/2/2.
 */

case class GroupMembers(members: Seq[Jid])

trait GroupDao {

  def members(groupId: String): Future[GroupMembers]
}
