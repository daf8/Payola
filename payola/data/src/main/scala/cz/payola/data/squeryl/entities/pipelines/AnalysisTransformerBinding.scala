/*package cz.payola.data.squeryl.entities.pipelines

import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl._

/**
 * This objects converts [[cz.payola.common.entities.pipelines.AnalysisTransformerBinding]]
 * to [[cz.payola.data.squeryl.entities.pipelines.AnalysisTransformerBinding]]
 */
object AnalysisTransformerBinding extends EntityConverter[AnalysisTransformerBinding]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[AnalysisTransformerBinding] = {
        entity match {
            case e: AnalysisTransformerBinding => Some(e)
            case e: cz.payola.common.entities.pipelines.AnalysisTransformerBinding => {
                val convertedBinding = new AnalysisTransformerBinding(e.id, Analysis(e.sourceAnalysis),
                    Transformer(e.targetTransformer))
                Some(convertedBinding)
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.pipelines.AnalysisTransformerBinding]] entities.
 * @param id ID of this binding
 * @param s Source analysis of this binding
 * @param t Target transformer of this binding
 * @param context Implicit context
 */
class AnalysisTransformerBinding(
    override val id: String,
    s: Analysis,
    t: Transformer)(implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.pipelines.AnalysisTransformerBinding(s, t)
    with Entity
{
    val sourceAnalysisId: String = Option(s).map(_.id).getOrElse(null)

    val targetTransformerId: String = Option(t).map(_.id).getOrElse(null)

    var pipelineId: String = null

    def sourceAnalysis_=(value: AnalysisType) {
        _sourceAnalysis = value
    }

    def targetTransformer_=(value: TransformerType) {
        _targetTransformer = value
    }
}
*/