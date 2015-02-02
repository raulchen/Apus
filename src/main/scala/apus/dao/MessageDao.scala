package apus.dao

import apus.channel.{GroupMessage, UserMessage}

import scala.concurrent.Future

/*
 * Created by Hao Chen on 2015/2/2.
 */

case class SavedUserMessage(m: UserMessage)
case class SavedGroupMessage(m: GroupMessage)

trait MessageDao {

  def saveUserMessage(m: UserMessage): Future[SavedUserMessage]

}
