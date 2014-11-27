package apus.util

/**
 * A bidirectional map
 * Created by Hao Chen on 2014/11/26.
 */
class BiMap[K, V] {

  private val keyMap = scala.collection.mutable.Map.empty[K, (K, V)]
  private val valueMap = scala.collection.mutable.Map.empty[V, (K, V)]

  def put(key: K, value: V): Unit ={
    val pair = (key, value)
    keyMap.put(key, pair)
    valueMap.put(value, pair)
  }

  def getValue(key: K): Option[V] = {
    keyMap.get(key).map(_._2)
  }

  def getKey(value: V): Option[K] = {
    valueMap.get(value).map(_._1)
  }

  def removeByKey(key: K): Unit ={
    keyMap.get(key).foreach{
      remove
    }
  }

  def removeByValue(value: V): Unit ={
    valueMap.get(value).foreach{
      remove
    }
  }

  private def remove(pair: (K, V)): Unit ={
    keyMap.remove(pair._1)
    valueMap.remove(pair._2)
  }
}
