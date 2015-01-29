import org.jboss.netty.handler.codec.base64.Base64

/**
 * Created by Hao Chen on 2014/11/15.
 */
object TempTest extends App{

  trait A1[T]{

    def get: T
  }

  trait A2[T]{

    this: A1[T] =>

    def print(): Unit = {
      val t = get
      println(t)
    }
  }

  class A3 extends A1[String] with A2[String]{

    override def get: String = "???"

  }

  new A3().print()

  trait B1{

    type T

    def get: T
  }

  trait B2{

    this: B1 =>

    def print(): Unit = {
      val t = get
      println(t)
    }
  }

  class B3 extends B1 with B2{

    type T = String

    override def get: String = "???"

  }

  new B3().print()

  import scala.concurrent.blocking

  blocking{

  }
}



