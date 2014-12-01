package apus.test

import apus.server.ClusteredXmppServer
import com.typesafe.config.{ConfigValueFactory, ConfigValue, ConfigFactory}

/**
 * Run cluster.
 * Created by Hao Chen on 2014/11/30.
 */
object ClusterTest{

  def start(port: Int, tcpPort: Int): Unit ={
    val config =
      ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
        .withValue("apus.server.port", ConfigValueFactory.fromAnyRef(tcpPort))
        .withFallback(ConfigFactory.load("cluster-test"))

    val server = new ClusteredXmppServer(config)
    server.startUp()
  }

  def main(args: Array[String]) {
    System.setProperty("java.library.path", "./sigar")
    start(2551, 5222)
    start(2552, 5223)
  }
}
