package apus.test

import apus.server.ClusteredXmppServer
import com.typesafe.config.{ConfigValueFactory, ConfigValue, ConfigFactory}

/**
 * Run cluster.
 * Created by Hao Chen on 2014/11/30.
 */
object ClusterTest{

  def start(incr: Int): Unit ={
    val config =
      ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${2551 + incr}")
        .withValue("apus.server.port", ConfigValueFactory.fromAnyRef(5222 + incr))
        .withFallback(ConfigFactory.load("cluster-test"))

    val server = new ClusteredXmppServer(config)
    server.startUp()
  }

  def main(args: Array[String]) {
    System.setProperty("java.library.path", "sigar")
//    start(0);
    start(1);
  }
}
