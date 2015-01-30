package apus.util.id

import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * Created by Hao Chen on 2015/1/19.
 */
class IdGenerator(uniqueCode: Int, versionNum: Int = 0) {

  @volatile private var curEpochSec = System.currentTimeMillis / 1000

  @volatile private var seq = new AtomicInteger(0)

  def nextId(flag: Int): Id = nextId(flag, System.currentTimeMillis / 1000)

  def nextId(flag: Int, epochSec: Long): Id = {
    var s = seq
    if(epochSec != curEpochSec){
      curEpochSec = epochSec
      seq = new AtomicInteger(0)
      s = seq
//      s.weakCompareAndSet()
    }
    Id(versionNum, uniqueCode, epochSec, s.incrementAndGet(), flag)
  }
}
