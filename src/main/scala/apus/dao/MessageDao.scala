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

//  def saveGroupMessage(m: GroupMessage): Future[SavedGroupMessage]

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

  val conf = new Configuration()
  conf.set("hbase.zookeeper.quorum", runtime.config.getString("hbase.zookeeper.quorum"))

  val tableFactory = () => {
    log.debug("Creating a new HTable instance")
    new HTable(conf, "apus_chat_msg")
  }

  val pool = {
    val strategy = OneForOneStrategy(/*maxNrOfRetries = 10, withinTimeRange = 1.minute*/) {
      case NonFatal(e) =>
        log.error(e, "An error occurred in MessageDaoActor")
        Restart
    }

    val size = runtime.config.getInt("apus.dao.msg.pool-size")

    val routeeProps = Props(classOf[MessageDaoActor], tableFactory)

    val props = RoundRobinPool(size, supervisorStrategy = strategy)
      .props(routeeProps)
      .withDispatcher("msg-dao-dispatcher")

    runtime.actorSystem.actorOf(props, name = "msgDao")
  }

  //Because it's very slow to create the first HTable instance due to zookeeper,
  //so we create a HTable and initialize zookeeper connection when the system starts.
  runtime.actorSystem.scheduler.scheduleOnce(Duration.Zero){
    tableFactory().close()
    log.info("connected to zookeeper")
  }(runtime.actorSystem.dispatcher)

  val breaker = {
    import com.github.kxbmap.configs._
    val maxFailures = runtime.config.get[Int]("apus.dao.msg.breaker.max-failures")
    val resetTimeout = runtime.config.get[Duration]("apus.dao.msg.breaker.reset-timeout")
      .asInstanceOf[FiniteDuration]
    CircuitBreaker(runtime.actorSystem.scheduler, maxFailures, timeout.duration, resetTimeout)
  }
  breaker.onOpen{
    log.warning("breaker opened")
  }

  override def saveUserMessage(m: UserMessage): Future[SavedUserMessage] = {
    breaker.withCircuitBreaker{
      (pool ? m).mapTo[SavedUserMessage]
    }
  }
}

class MessageDaoActor(tableFactory: () => HTable) extends Actor with ActorLogging {

  import org.apache.hadoop.hbase.util.Bytes.{toBytes => tb}

  lazy val table = tableFactory()

  override def receive: Receive = {
    case m: UserMessage =>
      withTry{
        saveUserMessage(m)
      }
  }

  /**
   * try a dangerous operation, reply result if the operation succeeds,
   * replay exception otherwise.
   */
  private def withTry(operation: => Any): Unit ={
    try {
      sender ! operation
    } catch {
      case e: Exception ⇒
        sender ! akka.actor.Status.Failure(e)
        throw e
    }
  }

  private def saveUserMessage(um: UserMessage): SavedUserMessage ={
//    throw new RuntimeException("hehe")
    val m = um.stanza
    val from = m.fromOpt.map(_.node).getOrElse("")
    val to = m.to.node

    val rowKey = s"$from-$to-" + (Long.MaxValue - System.currentTimeMillis())
    val put = new Put(tb(rowKey))
    put.add(tb("cf"), tb("from"), tb(from))
    put.add(tb("cf"), tb("to"), tb(to))
    put.add(tb("cf"), tb("body"), tb(m.body))
    table.put(put)

    SavedUserMessage(um)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    table.close()
  }
}