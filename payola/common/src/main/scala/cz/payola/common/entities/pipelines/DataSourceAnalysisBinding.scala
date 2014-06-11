package cz.payola.common.entities.pipelines

import cz.payola.common.Entity
import cz.payola.common.entities._
import cz.payola.common.entities.plugins.DataSource

/**
 * A binding between the output of analysis and the transformer.
 */
trait DataSourceAnalysisBinding extends Entity
{
    type DataSourceType <: DataSource

    type AnalysisType <: Analysis

    protected var _sourceDataSource: DataSourceType

    protected var _targetAnalysis: AnalysisType

    override def classNameText = "data source to analysis binding"

    def sourceDataSource = _sourceDataSource

    def targetAnalysis = _targetAnalysis
}
