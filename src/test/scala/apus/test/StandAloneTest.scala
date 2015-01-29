package apus.test

import apus.Main
import apus.server.StandAloneXmppServer
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

/**
 * Created by Hao Chen on 2014/11/30.
 */
object StandAloneTest {

  import com.github.kxbmap.configs._

  def main(args: Array[String]) {
    val config = ConfigFactory.load("stand-alone-test")
    val server = new StandAloneXmppServer(config)

    server.startUp()
    StdIn.readLine()
    server.shutDown()
  }

}
