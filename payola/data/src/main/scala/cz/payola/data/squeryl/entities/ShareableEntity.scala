package cz.payola.data.squeryl.entities

import cz.payola.domain.Entity

trait ShareableEntity extends Entity with cz.payola.domain.entities.ShareableEntity
{
    self: Entity =>

    // Set isPublic value into field that is persisted on DB
    var _isPub: Boolean

    // Restore publicity value from DB
    isPublic = _isPub
}
