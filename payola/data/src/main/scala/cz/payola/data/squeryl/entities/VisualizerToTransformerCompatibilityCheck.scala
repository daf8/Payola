package cz.payola.data.squeryl.entities

import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities._
import cz.payola._

/**
 * This objects converts [[VisualizerToTransformerCompatibilityCheck]]
 * to [[entities.VisualizerToTransformerCompatibilityCheck]]
 */
object VisualizerToTransformerCompatibilityCheck extends EntityConverter[VisualizerToTransformerCompatibilityCheck]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[VisualizerToTransformerCompatibilityCheck] = {
        entity match {
            case e: VisualizerToTransformerCompatibilityCheck => Some(e)
            case e: cz.payola.common.entities.VisualizerToTransformerCompatibilityCheck => {
                val convertedBinding = new VisualizerToTransformerCompatibilityCheck(e.id, e.visualizer,
                    Transformer(e.compatibleTransformer))
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
 * @param t Target data source of this binding
 * @param context Implicit context
 */
class VisualizerToTransformerCompatibilityCheck(
    override val id: String,
    visualizer: String,
    t: Transformer
    )(implicit val context: SquerylDataContextComponent)
    extends domain.entities.VisualizerToTransformerCompatibilityCheck(visualizer, t)
    with Entity
{
    val compatibleTransformerId: String = Option(t).map(_.id).getOrElse(null)

    var visualizerId: String = null

    def visualizer_=(value: String) {
        _visualizer = value
    }

    def compatibleTransformer_=(value: TransformerType) {
        _compatibleTransformer = value
    }
}
