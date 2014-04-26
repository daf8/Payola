package cz.payola.domain.entities.transformers

import cz.payola.domain.Entity
import cz.payola.domain.entities.plugins.TransformerPluginInstance

/**
 * @param _sourcePluginInstance The plugin instance that acts as a source of the binding.
 * @param _targetPluginInstance The plugin instance that acts as a target of the binding.
 * @param _targetInputIndex Index of the target plugin instance input the binding is connected to.
 */
class TransformerPluginInstanceBinding(
    protected var _sourcePluginInstance: TransformerPluginInstance,
    protected var _targetPluginInstance: TransformerPluginInstance,
    protected val _targetInputIndex: Int = 0)
    extends Entity
    with cz.payola.common.entities.transformers.TransformerPluginInstanceBinding
{
    checkConstructorPostConditions()

    type TransformerPluginInstanceType = TransformerPluginInstance

    override def canEqual(other: Any): Boolean = {
        other.isInstanceOf[TransformerPluginInstanceBinding]
    }

    override protected def checkInvariants() {
        super[Entity].checkInvariants()
        validate(sourcePluginInstance != null, "sourcePluginInstance",
            "The source plugin instance of the binding mustn't be null.")
        validate(targetPluginInstance != null, "targetPluginInstance",
            "The target plugin instance of the binding mustn't be null.")
        validate(sourcePluginInstance != targetPluginInstance, "sourcePluginInstance",
            "The source plugin instance of the binding cannot also be the target plugin instance (a cycle formed of " +
                "one plugin instance).")
        validate(targetInputIndex >= 0 && targetInputIndex < targetPluginInstance.plugin.inputCount,
            "targetInputIndex", "The target input index of the binding is invalid.")
    }
}
