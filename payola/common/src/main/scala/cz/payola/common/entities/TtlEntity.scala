package cz.payola.common.entities

import cz.payola.common.Entity

/**
  * A generic entity with turtle RDF sample data.
  */
trait TtlEntity extends Entity
{
    self: Entity =>

    protected var _ttl: String = ""

    /** turtle RDF sample of the entity */
    def ttl = _ttl

    /**
      * Sets turtle RDF sampla of the entity.
      * @param value The value of new RDF sample.
      */
    def ttl_=(value: String) {
        _ttl = value
    }
}
