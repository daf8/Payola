/*package cz.payola.data.squeryl.entities.pipelines

import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl._

/**
 * This objects converts [[cz.payola.common.entities.pipelines.AnalysisVisualizerBinding]]
 * to [[cz.payola.data.squeryl.entities.pipelines.AnalysisVisualizerBinding]]
 */
object AnalysisVisualizerBinding extends EntityConverter[AnalysisVisualizerBinding]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[AnalysisVisualizerBinding] = {
        entity match {
            case e: AnalysisVisualizerBinding => Some(e)
            case e: cz.payola.common.entities.pipelines.AnalysisVisualizerBinding => {
                val convertedBinding = new AnalysisVisualizerBinding(e.id, Analysis(e.sourceAnalysis),
                    Visualizer(e.targetVisualizer))
                Some(convertedBinding)
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.pipelines.AnalysisVisualizerBinding]] entities.
 * @param id ID of this binding
 * @param s Source analysis of this binding
 * @param t Target visualizer of this binding
 * @param context Implicit context
 */
class AnalysisVisualizerBinding(
    override val id: String,
    s: Analysis,
    t: Visualizer)(implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.pipelines.AnalysisVisualizerBinding(s, t)
    with Entity
{
    val sourceAnalysisId: String = Option(s).map(_.id).getOrElse(null)

    val targetVisualizerId: String = Option(t).map(_.id).getOrElse(null)

    var pipelineId: String = null

    def sourceAnalysis_=(value: AnalysisType) {
        _sourceAnalysis = value
    }

    def targetVisualizer_=(value: VisualizerType) {
        _targetVisualizer = value
    }
}
*/