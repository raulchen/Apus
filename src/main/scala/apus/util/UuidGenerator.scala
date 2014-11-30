package apus.util

import java.util.UUID

/**
 * Created by Hao Chen on 2014/11/25.
 */
object UuidGenerator {

  /**
   * generate an UUID String without dashes, thread safe
   * @return
   */
  def next(): String = {
    //todo do it without synchronized
    UUID.randomUUID.toString.replaceAllLiterally("-", "")
  }
}
