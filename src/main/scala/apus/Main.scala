package apus

import java.io.File

import apus.server.{XmppServer, ClusteredXmppServer, StandAloneXmppServer}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

/**
 * Created by Hao Chen on 2014/11/5.
 */
//object Main {
//
//  import com.github.kxbmap.configs._
//
//  def main(args: Array[String]) {
//    val config = if (args.isEmpty){
//      ConfigFactory.load()
//    } else{
//      ConfigFactory.load(args(0))
//    }
//
//    val mode = config.opt[String]("apus.mode")
//
//    val server = mode match{
//      case Some("stand-alone") => Some(new StandAloneXmppServer(config))
//      case Some("cluster") => Some(new ClusteredXmppServer(config))
//      case _ => {
//        System.err.println("""'apus.mode' should be set to either 'stand-alone' or 'cluster' in config file.""")
//        System.exit(-1)
//        None
//      }
//    }
//
//    server.foreach { s =>
//      s.startUp()
//      StdIn.readLine()
//      s.shutDown()
//    }
//  }
//}
object Main {

  import com.github.kxbmap.configs._

  def main(args: Array[String]) {
    System.setProperty("java.library.path", args(0) + "/sigar")
    val config = ConfigFactory.parseFile(new File(args(0) + "/apus.conf"))

    val mode = config.opt[String]("apus.mode")

    val server = mode match{
      case Some("stand-alone") => Some(new StandAloneXmppServer(config))
      case Some("cluster") => Some(new ClusteredXmppServer(config))
      case _ => {
        System.err.println("""'apus.mode' should be set to either 'stand-alone' or 'cluster' in config file.""")
        System.exit(-1)
        None
      }
    }

    server.foreach { s =>
      s.startUp()
      StdIn.readLine()
      s.shutDown()
    }
  }
}