package cz.payola.data.squeryl

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.CompositeKey2

/**
 * Defines member-relation between pipeline and data source
 * @param pipelineId Id of the pipeline
 * @param dsId  Id of the data source
 */
class PipelineDataSource(val dsId: String, val pipelineId: String)
    extends KeyedEntity[CompositeKey2[String, String]]
{
    def id = compositeKey(dsId, pipelineId)
}

