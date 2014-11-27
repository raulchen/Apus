package apus.session.handlers

import apus.protocol.Stanza

/**
 * Stanza handler.
 * Created by Hao Chen on 2014/11/24.
 */
trait StanzaHandler[T <: Stanza] {

  def handle(stanza: T): Unit
}
