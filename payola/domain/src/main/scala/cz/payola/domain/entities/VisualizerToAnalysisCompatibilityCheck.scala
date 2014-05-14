package cz.payola.domain.entities

import cz.payola.domain.Entity

/**
 * @param _visualizer The plugin instance that acts as a source of the binding.
 * @param _compatibleAnalysis The plugin instance that acts as a target of the binding.
 */
class VisualizerToAnalysisCompatibilityCheck(
    protected var _visualizer: String,
    protected var _compatibleAnalysis: Analysis)
    extends Entity
    with cz.payola.common.entities.VisualizerToAnalysisCompatibilityCheck
{
    checkConstructorPostConditions()

    type AnalysisType = Analysis

    override def canEqual(other: Any): Boolean = {
        other.isInstanceOf[VisualizerToAnalysisCompatibilityCheck]
    }

    override protected def checkInvariants() {
        super[Entity].checkInvariants()
        validate(visualizer != null, "visualizer",
            "The visualizer of the binding mustn't be null.")
        validate(_compatibleAnalysis != null, "compatibleAnalysis",
            "The compatible analysis of the binding mustn't be null.")
    }
}
