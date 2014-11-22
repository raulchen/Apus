package apus.protocol

/*
 * Created by Hao Chen on 2014/11/19.
 */


object SaslMechanism extends Enumeration
{
  type mechanism = Value

  // TODO: add more
  val Plain = Value("PLAIN")
  val DiagestMD5 = Value("DIGEST-MD5")
  val External = Value("EXTERNAL")
}