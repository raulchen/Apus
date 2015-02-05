package apus.server

import apus.auth.{AnonymousUserAuth, UserAuth}
import apus.channel.UserMessage
import apus.dao._
import apus.protocol.Jid

import scala.concurrent.Future

/**
 * Created by Hao Chen on 2015/2/2.
 */
object MockDataAccess extends DataAccess{

  override def userAuth: UserAuth = AnonymousUserAuth

  override def messageDao: MessageDao = MockMessageDao

  override def groupDao: GroupDao = MockGroupDao
}

object MockMessageDao extends MessageDao {

  override def saveUserMessage(m: UserMessage): Future[SavedUserMessage] = {
    Future.successful(SavedUserMessage(m))
  }
}

object MockGroupDao extends GroupDao {

  val jidList = (1 to 5).map(id => Jid(id + "@apus.im"))

  override def members(groupId: String): Future[GroupMembers] = {
    Future.successful(GroupMembers(jidList))
  }
}