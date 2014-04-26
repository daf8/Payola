package cz.payola.domain.entities

import cz.payola.domain.Entity
import java.sql.Timestamp

class TransformerResult(
    protected override var transformerid: String,
    protected override var _owner: Option[User],
    protected override var evaluationid: String,
    protected override var verticescount: Int,
    protected var _touched: Timestamp)
    extends Entity with cz.payola.common.entities.TransformerResult
    with OptionallyOwnedEntity with NamedEntity
{

    override var _name = "http://"+transformerid+"/"+evaluationid

    override def name = "http://"+transformerid+"/"+evaluationid

    override def name_=(value: String) {
        //ain't gonna do a thing
    }

    override def touched = new java.util.Date(_touched.getTime())

    override def touched_=(value: java.util.Date) {
        _touched = new java.sql.Timestamp(value.getTime)
    }

    /**
     * Sets the owner of the transformerResult.
     * @param value The new owner of the transformerResult.
     */
    override def owner_=(value: Option[UserType]) {
        _owner = value
        super[OptionallyOwnedEntity].checkInvariants()
    }

    override final def canEqual(other: Any): Boolean = {
        other.isInstanceOf[TransformerResult]
    }

    override protected final def checkInvariants() {
        super[Entity].checkInvariants()

        validate(transformerid.length > 0, "transformerid", "Transformerid has to be specified")
        validate(evaluationid.length > 0, "evaluationid", "Evaluationid has to be specified")
    }
}