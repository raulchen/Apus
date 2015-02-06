package apus.network

/*
 * Created by Hao Chen on 2014/11/5.
 */

/**
 * Socket endpoint
 */
trait Endpoint {

  def start()

  def shutdown()
}
