package apus.util

import java.util.UUID

/**
 * Created by Hao Chen on 2014/11/25.
 */
class UuidGenerator {

  /**
   * generate an UUID String without dashes
   * @return
   */
  def next(): String = {
    UUID.randomUUID.toString.replaceAllLiterally("-", "")
  }
}
