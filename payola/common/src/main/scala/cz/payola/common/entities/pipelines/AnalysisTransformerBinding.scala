package cz.payola.common.entities.pipelines

import cz.payola.common.Entity
import cz.payola.common.entities._

/**
 * A binding between the output of analysis and the transformer.
 */
trait AnalysisTransformerBinding extends Entity
{
    type AnalysisType <: Analysis

    type TransformerType <: Transformer

    protected var _sourceAnalysis: AnalysisType

    protected var _targetTransformer: TransformerType

    override def classNameText = "analysis to transformer binding"

    def sourceAnalysis = _sourceAnalysis

    def targetTransformer = _targetTransformer
}
