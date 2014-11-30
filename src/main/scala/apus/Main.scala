package apus

import apus.server.{ClusteredXmppServer, StandAloneXmppServer}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

/**
 * Created by Hao Chen on 2014/11/5.
 */
object Main {

  def main(args: Array[String]) {
    val config = if (args.isEmpty){
      ConfigFactory.load()
    } else{
      ConfigFactory.load(args(0))
    }

    var mode: String = null
    if(config.hasPath("apus.mode")){
      mode = config.getString("apus.mode").toLowerCase()
      if(mode != "stand-alone" &&
          mode != "cluster"){
        mode = null
      }
    }

    if(mode == null){
      System.err.println("""'apus.mode' should be set to either 'stand-alone' or 'cluster' in config file.""")
      System.exit(-1)
    }

    val server = mode match{
      case "stand-alone" => new StandAloneXmppServer(config)
      case "cluster" => new ClusteredXmppServer(config)
    }

    server.startUp()
    StdIn.readLine()
    server.shutDown()
  }
}
