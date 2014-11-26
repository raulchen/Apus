package apus.session.handlers

import apus.protocol.Stanza

/**
 * Created by Hao Chen on 2014/11/24.
 */
trait StanzaHandler[T <: Stanza] {

  def handle(stanza: T): Unit
}
