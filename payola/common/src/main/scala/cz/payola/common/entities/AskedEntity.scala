package cz.payola.common.entities

import cz.payola.common.Entity

/**
  * A ASK query described generic entity.
  */
trait AskedEntity extends Entity
{
    self: Entity =>

    protected var _ask: String = ""

    /** ASK query of the entity */
    def ask = _ask

    /**
      * Sets ASK query of the entity.
      * @param value The value of new ASK query.
      */
    def ask_=(value: String) {
        _ask = value
    }
}
