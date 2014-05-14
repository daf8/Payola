package cz.payola.data.squeryl.entities

import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities._
import cz.payola._

/**
 * This objects converts [[VisualizerToAnalysisCompatibilityCheck]]
 * to [[entities.VisualizerToAnalysisCompatibilityCheck]]
 */
object VisualizerToAnalysisCompatibilityCheck extends EntityConverter[VisualizerToAnalysisCompatibilityCheck]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[VisualizerToAnalysisCompatibilityCheck] = {
        entity match {
            case e: VisualizerToAnalysisCompatibilityCheck => Some(e)
            case e: cz.payola.common.entities.VisualizerToAnalysisCompatibilityCheck => {
                val convertedBinding = new VisualizerToAnalysisCompatibilityCheck(e.id, e.visualizer,
                    Analysis(e.compatibleAnalysis))
                Some(convertedBinding)
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[domain.entities.tr.TransformerCompatibilityCheck]] entities.
 * @param id ID of this binding
 * @param visualizer Source plugin instance of this binding
 * @param a Target data source of this binding
 * @param context Implicit context
 */
class VisualizerToAnalysisCompatibilityCheck(
    override val id: String,
    visualizer: String,
    a: Analysis
    )(implicit val context: SquerylDataContextComponent)
    extends domain.entities.VisualizerToAnalysisCompatibilityCheck(visualizer, a)
    with Entity
{
    val compatibleAnalysisId: String = Option(a).map(_.id).getOrElse(null)

    var visualizerId: String = null

    def visualizer_=(value: String) {
        _visualizer = value
    }

    def compatibleAnalysis_=(value: AnalysisType) {
        _compatibleAnalysis = value
    }
}
