package cz.payola.common.entities.pipelines

import cz.payola.common.Entity
import cz.payola.common.entities._

/**
 * A binding between the output of analysis and the transformer.
 */
trait TransformerVisualizerBinding extends Entity
{
    type TransformerType <: Transformer

    type VisualizerType <: Visualizer

    protected var _sourceTransformer: TransformerType

    protected var _targetVisualizer: VisualizerType

    override def classNameText = "transformer to visualizer binding"

    def sourceTransformer = _sourceTransformer

    def targetVisualizer = _targetVisualizer
}
