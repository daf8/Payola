package cz.payola.common.entities.pipelines

import cz.payola.common.Entity
import cz.payola.common.entities._

/**
 * A binding between the output of analysis and the transformer.
 */
trait TransformerTransformerBinding extends Entity
{
    type TransformerType <: Transformer

    protected var _sourceTransformer: TransformerType

    protected var _targetTransformer: TransformerType

    override def classNameText = "transformer to transformer binding"

    def sourceTransformer = _sourceTransformer

    def targetTransformer = _targetTransformer
}
