
import java.io.File
import java.util

import apus.protocol.{ServerResponses, Message, Jid}
import apus.session.SessionState
import apus.util.Xml
import com.fasterxml.aalto.{AsyncXMLStreamReader, AsyncXMLInputFactory}
import com.fasterxml.aalto.evt.EventAllocatorImpl
import com.fasterxml.aalto.stax.InputFactoryImpl

import scala.collection.JavaConverters._
import scala.io.{StdIn, Source}
import scala.reflect.macros.blackbox
import scala.xml.XML
import scala.xml.pull.XMLEventReader
import scala.concurrent.duration._

/**
 * Created by Hao Chen on 2014/11/15.
 */
object TempTest extends App{

  var x: String = _

  println(x)

//
//  class A(val x: Int) {
//
//    def x2 = x*x
//  }
//
//  case class B(var b: Int)
//
//  trait Age{
//    def age(): Int
//  }
//
//  trait DoubleAge extends Age{
//    abstract override def age() = super.age() * 2
//  }
//
//  case class Person(override val age: Int) extends DoubleAge
//
//  trait MyToString{
//    override def toString: String = s"hehe-${super.toString}"
//  }
//
//  object Log
//  {
//    def apply(msg: String): Unit = macro applyImpl
//    def applyImpl(c: blackbox.Context)(msg: c.Expr[String]):c.Expr[Unit] =
//    {
//      import c.universe._
//      val tree = q"""if (Log.enabled) {
//                      Log.log(${msg})
//                  }
//               """
//      c.Expr[Unit](tree)
//    }
//  }
//
//  def main(args: Array[String]) {
//    val p = new Person(1)
//    println(p.age)
//  }
//

}
