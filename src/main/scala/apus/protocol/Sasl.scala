package apus.protocol

/**
 * useless for now.
 * Created by Hao Chen on 2014/11/19.
 */
object SaslMechanism extends Enumeration
{
  type mechanism = Value

  val Plain = Value("PLAIN")
  val DigestMD5 = Value("DIGEST-MD5")
  val External = Value("EXTERNAL")
}