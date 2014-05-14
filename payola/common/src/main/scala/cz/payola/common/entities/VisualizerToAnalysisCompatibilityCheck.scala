package cz.payola.common.entities

import cz.payola.common.Entity
import cz.payola.common.entities.plugins._

/**
 * A binding between the output of one plugin instance and the specified input of another plugin instance.
 */
trait VisualizerToAnalysisCompatibilityCheck extends Entity
{
    /** Type of the plugin instances the current binding is between. */
    type AnalysisType <: Analysis

    protected var _visualizer: String

    protected var _compatibleAnalysis: AnalysisType

    override def classNameText = "compatibility check"

    /** The plugin instance that acts as a source of the binding (the binding is connected to its output). */
    def visualizer = _visualizer

    /** The plugin instance that acts as a target of the binding (the binding is connected to its input). */
    def compatibleAnalysis = _compatibleAnalysis

}
