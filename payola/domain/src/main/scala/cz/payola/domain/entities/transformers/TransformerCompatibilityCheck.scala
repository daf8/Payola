package cz.payola.domain.entities.transformers

import cz.payola.common.entities.analyses
import cz.payola.common.entities.plugins.DataSource
import cz.payola.domain.Entity
import cz.payola.domain.entities._
import cz.payola.domain.entities.plugins._

/**
 * @param _sourcePluginInstance The plugin instance that acts as a source of the binding.
 * @param _compatibleAnalysis The plugin instance that acts as a target of the binding.
 */
class TransformerCompatibilityCheck(
    protected var _sourcePluginInstance: TransformerPluginInstance,
    protected var _compatibleAnalysis: Analysis)
    extends Entity
    with cz.payola.common.entities.transformers.TransformerCompatibilityCheck
{
    checkConstructorPostConditions()

    type TransformerPluginInstanceType = TransformerPluginInstance

    type AnalysisType = Analysis

    override def canEqual(other: Any): Boolean = {
        other.isInstanceOf[TransformerCompatibilityCheck]
    }

    override protected def checkInvariants() {
        super[Entity].checkInvariants()
        validate(sourcePluginInstance != null, "sourcePluginInstance",
            "The source plugin instance of the binding mustn't be null.")
        validate(_compatibleAnalysis != null, "compatibleAnalysis",
            "The compatible analysis of the binding mustn't be null.")
        validate(sourcePluginInstance != _compatibleAnalysis, "sourcePluginInstance",
            "The source plugin instance of the binding cannot also be the target plugin instance (a cycle formed of " +
                "one plugin instance).")
    }
}
