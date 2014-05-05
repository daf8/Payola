package cz.payola.domain.entities.transformers

import cz.payola.common.entities.analyses
import cz.payola.common.entities.plugins.DataSource
import cz.payola.domain.Entity
import cz.payola.domain.entities._
import cz.payola.domain.entities.plugins._

/**
 * @param _sourcePluginInstance The plugin instance that acts as a source of the binding.
 * @param _compatibleTransformer The plugin instance that acts as a target of the binding.
 */
class TransformerToTransformerCompatibilityCheck(
    protected var _sourcePluginInstance: TransformerPluginInstance,
    protected var _compatibleTransformer: Transformer)
    extends Entity
    with cz.payola.common.entities.transformers.TransformerToTransformerCompatibilityCheck
{
    checkConstructorPostConditions()

    type TransformerPluginInstanceType = TransformerPluginInstance

    type TransformerType = Transformer

    override def canEqual(other: Any): Boolean = {
        other.isInstanceOf[TransformerToTransformerCompatibilityCheck]
    }

    override protected def checkInvariants() {
        super[Entity].checkInvariants()
        validate(sourcePluginInstance != null, "sourcePluginInstance",
            "The source plugin instance of the binding mustn't be null.")
        validate(_compatibleTransformer != null, "compatibleTransformer",
            "The compatible transformer of the binding mustn't be null.")
        validate(sourcePluginInstance != _compatibleTransformer, "sourcePluginInstance",
            "The source plugin instance of the binding cannot also be the target plugin instance (a cycle formed of " +
                "one plugin instance).")
    }
}
