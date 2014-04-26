package cz.payola.common.entities

import cz.payola.common.Entity

trait TransformerResult extends Entity with OptionallyOwnedEntity with NamedEntity {
    protected var verticescount: Int
    protected var transformerid: String
    protected var evaluationid: String

    def touched: java.util.Date = null

    def touched_=(value: java.util.Date) {}

    def evaluationId = evaluationid

    def evaluationId_=(value: String) {
        evaluationid = value
    }

    def verticesCount = verticescount

    def  verticesCount_=(value: Int) {
        verticescount = value
    }

    def transformerId = transformerid

    def transformerId_=(value: String) {
        transformerid = value
    }
}