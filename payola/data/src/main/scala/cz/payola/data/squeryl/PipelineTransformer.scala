package cz.payola.data.squeryl

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.CompositeKey2

/**
 * Defines member-relation between pipeline and data source
 * @param pipelineId Id of the pipeline
 * @param transformerId  Id of the data source
 */
class PipelineTransformer(val pipelineId: String, val transformerId: String)
    extends KeyedEntity[CompositeKey2[String, String]]
{
    def id = compositeKey(pipelineId, transformerId)
}

