package apus.util

import java.util.UUID

/**
 * Created by Hao Chen on 2014/11/25.
 */
class UuidGenerator {

  def next(): String = {
    UUID.randomUUID.toString.replaceAllLiterally("-","")
  }
}
