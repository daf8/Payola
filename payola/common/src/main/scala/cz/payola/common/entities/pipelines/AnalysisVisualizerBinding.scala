package cz.payola.common.entities.pipelines

import cz.payola.common.Entity
import cz.payola.common.entities._

/**
 * A binding between the output of analysis and the visualizer.
 */
trait AnalysisVisualizerBinding extends Entity
{
    type AnalysisType <: Analysis

    type VisualizerType <: Visualizer

    protected var _sourceAnalysis: AnalysisType

    protected var _targetVisualizer: VisualizerType

    override def classNameText = "analysis to visualizer binding"

    def sourceAnalysis = _sourceAnalysis

    def targetVisualizer = _targetVisualizer
}
