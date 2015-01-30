package apus.util.id

import java.nio.ByteBuffer
import java.time._
import java.util.Base64


/**
 * A 64-bit Id
 * Created by Hao Chen on 2015/1/19.
 */
case class Id(bits: Long) {

  import Id._
  import IdVersion._

  /**
   * the version number
   */
  val versionNum = extractBits(bits, VersionOffset, VersionLen)
  private val version = IdVersion(versionNum)

  import version._

  /**
   * @return the unique code of the generator who generated this Id
   */
  def uniqueCode = extractBits(bits, uniqueCodeOffset, uniqueCodeLen)

  /**
   * @return seconds elapsed since the start time
   */
  def secElapsed = extractBits(bits, secElapsedOffset, secElapsedLen)

  /**
   * @return the time when this Id is generated
   */
  def time = Instant.ofEpochSecond(startTimeEpochSec + secElapsed)

  /**
   * @return the sequence number
   */
  def seq = extractBits(bits, seqOffset, seqLen)

  /**
   * @return the flag
   */
  def flag = extractBits(bits, flagOffset, flagLen)

  def toHexString: String = {
    bits.toHexString
  }

  def toBase64Sting: String = {
    val buf = ByteBuffer.allocate(java.lang.Long.BYTES).putLong(bits)
    base64Encoder.encodeToString(buf.array())
  }

  override def toString: String = toHexString
}

object Id{

  private val base64Encoder = Base64.getEncoder

  private def extractBits(bits: Long, from: Int, len: Int): Int = {
    val mask = (1L << len) - 1
    ((bits >>> from) & mask).toInt
  }

  private def set(bits: Long, value: Int, from: Int, len: Int): Long ={
    val mask = (1L << len) - 1
    bits | ((value & mask) << from)
  }

  /**
   * create a Id instance
   * @param versionNum the version number
   * @param uniqueCode the unique code of the id generator
   * @param epochSec seconds since epoch
   * @param seq the sequence number of this second
   * @param flag the flag
   * @return
   */
  def apply(versionNum: Int, uniqueCode: Int, epochSec: Long, seq: Int, flag: Int): Id = {
    var bits = 0L
    val ver = IdVersion(versionNum)
    import ver._
    import IdVersion._

    val secElapsed = epochSec - startTimeEpochSec
    require(secElapsed >= 0 && secElapsed < secElapsedUpperBound,
      "invalid epochSec: " + epochSec)

    bits = set(bits, ver.versionNum, VersionOffset, VersionLen)
    bits = set(bits, uniqueCode, uniqueCodeLen, uniqueCodeOffset)
    bits = set(bits, secElapsed.toInt, secElapsedLen, secElapsedOffset)
    bits = set(bits, seq, seqLen, seqOffset)
    bits = set(bits, flag, flagLen, flagOffset)

    Id(bits)
  }
}


/**
 * represent the version of a Id.
 * the length of a filed cannot be larger than 32
 */
trait IdVersion {

  import Id._
  import IdVersion._

  val startTime: Instant

  val startTimeEpochSec = startTime.getEpochSecond

  val versionNum: Int

  val uniqueCodeLen: Int

  val secElapsedLen: Int

  val seqLen: Int

  val flagLen: Int

  val uniqueCodeOffset = VersionLen
  
  val secElapsedOffset = uniqueCodeOffset + uniqueCodeLen

  val seqOffset = secElapsedOffset + secElapsedLen

  val flagOffset = seqOffset + seqLen

  require(flagOffset + flagLen == 64)

  val secElapsedUpperBound = 1 << secElapsedLen

  val seqUpperBound = 1 << seqLen
}

object IdVersion {

  val VersionOffset = 0

  val VersionLen = 2

  private[id] val Version0 = new IdVersion {

    override val versionNum: Int = 0

    override val startTime: Instant = ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant

    override val uniqueCodeLen: Int = 6

    override val secElapsedLen: Int = 30

    override val seqLen: Int = 24

    override val flagLen: Int = 2

  }

  def apply(versionNum: Int) = {
    versionNum match {
      case 0 => Version0
      case _ => throw new IllegalArgumentException(s"unsupported version: $versionNum")
    }
  }
}

object IdFlag {

  val Chat = 0

  val GroupChat = 1

  val Other = 3
}