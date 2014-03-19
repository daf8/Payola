package cz.payola.common.entities

import cz.payola.common.Entity

/**
 * A generic entity with checked ASK query.
 */
trait CheckedEntity extends Entity
{
    self: Entity =>

    protected var _checked: Boolean = false

    def checked = _checked

    def checked_=(value: Boolean) {
        _checked = value
    }

    protected var _lastCheck: Long = 0

    def lastCheck = _lastCheck

    def lastCheck_=(value: Long) {
        _lastCheck = value
    }

}
