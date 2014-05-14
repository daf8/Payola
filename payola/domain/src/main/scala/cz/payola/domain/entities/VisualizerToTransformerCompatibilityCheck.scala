package cz.payola.domain.entities

import cz.payola.domain.Entity

/**
 * @param _visualizer The plugin instance that acts as a source of the binding.
 * @param _compatibleTransformer The plugin instance that acts as a target of the binding.
 */
class VisualizerToTransformerCompatibilityCheck(
    protected var _visualizer: String,
    protected var _compatibleTransformer: Transformer)
    extends Entity
    with cz.payola.common.entities.VisualizerToTransformerCompatibilityCheck
{
    checkConstructorPostConditions()

    type TransformerType = Transformer

    override def canEqual(other: Any): Boolean = {
        other.isInstanceOf[VisualizerToTransformerCompatibilityCheck]
    }

    override protected def checkInvariants() {
        super[Entity].checkInvariants()
        validate(visualizer != null, "visualizer",
            "The visualizer of the binding mustn't be null.")
        validate(_compatibleTransformer != null, "compatibleTransformer",
            "The compatible transformer of the binding mustn't be null.")
    }
}
