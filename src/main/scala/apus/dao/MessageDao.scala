package apus.dao

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import akka.event.Logging
import akka.pattern.CircuitBreaker
import akka.routing.RoundRobinPool
import akka.util.Timeout
import apus.channel.{GroupMessage, UserMessage}
import apus.server.ServerRuntime
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{HTable, Put}

import scala.concurrent.Future
import scala.util.control.NonFatal

/*
 * Created by Hao Chen on 2015/2/2.
 */

case class SavedUserMessage(m: UserMessage)
case class SavedGroupMessage(m: GroupMessage)

trait MessageDao {

  def saveUserMessage(m: UserMessage): Future[SavedUserMessage]

  def saveGroupMessage(m: GroupMessage): Future[SavedGroupMessage]

}

class MessageDaoImpl(runtime: ServerRuntime) extends MessageDao {

  import akka.pattern.ask

  import scala.concurrent.duration._

  implicit val timeout: Timeout = {
    import com.github.kxbmap.configs._
    val timeout = runtime.config.get[Duration]("apus.dao.msg.timeout")
    Timeout(timeout.asInstanceOf[FiniteDuration])
  }

  val log = Logging(runtime.actorSystem, this.getClass)

  val hbaseConf = {
    val conf = new Configuration()
    conf.set("hbase.zookeeper.quorum", runtime.config.getString("hbase.zookeeper.quorum"))
    conf
  }

  val userMessageTableFactory = () => {
    log.debug("Creating a new HTable instance for UserMessage")
    new HTable(hbaseConf, "apus_user_msg")
  }

  val groupMessageTableFactory = () => {
    log.debug("Creating a new HTable instance for GroupMessage")
    new HTable(hbaseConf, "apus_group_msg")
  }

  //Because it's very slow to create the first HTable instance due to zookeeper,
  //so we create a HTable and initialize zookeeper connection here as soon as the system starts.
  runtime.actorSystem.scheduler.scheduleOnce(Duration.Zero){
    userMessageTableFactory().close()
    log.info("connected to zookeeper")
  }(runtime.actorSystem.dispatcher)

  val userMessageDaoPool = createMessageDaoPool("userMessageDaoPool",
    Props(classOf[UserMessageDaoActor], userMessageTableFactory))

  val groupMessageDaoPool = createMessageDaoPool("groupMessageDaoPool",
    Props(classOf[GroupMessageDaoActor], groupMessageTableFactory))

  private def createMessageDaoPool(poolName: String, routeeProps: Props) = {
    val strategy = OneForOneStrategy(/*maxNrOfRetries = 10, withinTimeRange = 1.minute*/) {
      case NonFatal(e) =>
        log.error(e, "An error occurred in MessageDaoActor")
        Restart
    }

    val size = runtime.config.getInt("apus.dao.msg.pool-size")
    val props = RoundRobinPool(size, supervisorStrategy = strategy)
      .props(routeeProps)
      .withDispatcher("msg-dao-dispatcher")

    runtime.actorSystem.actorOf(props, name = poolName)
  }

  val breaker = {
    import com.github.kxbmap.configs._
    val maxFailures = runtime.config.get[Int]("apus.dao.msg.breaker.max-failures")
    val resetTimeout = runtime.config.get[Duration]("apus.dao.msg.breaker.reset-timeout")
      .asInstanceOf[FiniteDuration]

    val breaker = CircuitBreaker(runtime.actorSystem.scheduler,
      maxFailures, timeout.duration, resetTimeout)
    breaker.onOpen{
      log.warning("Breaker opened")
    }
    breaker.onClose{
      log.info("Breaker closed")
    }

    breaker
  }


  override def saveUserMessage(m: UserMessage): Future[SavedUserMessage] = {
    breaker.withCircuitBreaker{
      (userMessageDaoPool ? m).mapTo[SavedUserMessage]
    }
  }

  override def saveGroupMessage(m: GroupMessage): Future[SavedGroupMessage] = {
    breaker.withCircuitBreaker{
      (groupMessageDaoPool ? m).mapTo[SavedGroupMessage]
    }
  }
}

abstract class MessageDaoActor(tableFactory: () => HTable) extends Actor with ActorLogging {

  lazy val table = tableFactory()

  /**
   * try a dangerous operation, reply result if the operation succeeds,
   * replay exception otherwise.
   */
  protected def withTry(operation: => Any): Unit ={
    try {
      sender ! operation
    } catch {
      case e: Exception ⇒
        sender ! akka.actor.Status.Failure(e)
        throw e
    }
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    table.close()
  }
}

class UserMessageDaoActor(tableFactory: () => HTable) extends MessageDaoActor(tableFactory) {

  import org.apache.hadoop.hbase.util.Bytes.{toBytes => tb}

  override def receive: Receive = {
    case um: UserMessage =>
      withTry{
        saveUserMessage(um)
      }
  }

  private def saveUserMessage(um: UserMessage): SavedUserMessage = {
//    throw new RuntimeException("hehe")
    val m = um.stanza
    val from = m.fromOpt.map(_.node).getOrElse("")
    val to = m.to.node

    val rowKey = s"$from#$to#" + (Long.MaxValue - System.currentTimeMillis())
    val put = new Put(tb(rowKey))
    put.add(tb("cf"), tb("from"), tb(from))
    put.add(tb("cf"), tb("to"), tb(to))
    put.add(tb("cf"), tb("body"), tb(m.body))
    table.put(put)

    SavedUserMessage(um)
  }
}

class GroupMessageDaoActor(tableFactory: () => HTable) extends MessageDaoActor(tableFactory) {

  import org.apache.hadoop.hbase.util.Bytes.{toBytes => tb}

  override def receive: Receive = {
    case gm: GroupMessage =>
      withTry{
        saveGroupMessage(gm)
      }
  }

  private def saveGroupMessage(gm: GroupMessage): SavedGroupMessage = {
    //    throw new RuntimeException("hehe")
    val m = gm.stanza
    val groupId = gm.groupId
    val rowKey = s"$groupId#" + (Long.MaxValue - System.currentTimeMillis())
    val put = new Put(tb(rowKey))
    put.add(tb("cf"), tb("groupId"), tb(groupId))
    put.add(tb("cf"), tb("from"), tb(m.fromOpt.flatMap(_.resourceOpt).getOrElse("")))
    put.add(tb("cf"), tb("body"), tb(m.body))
    table.put(put)

    SavedGroupMessage(gm)
  }
}