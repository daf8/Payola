/*package cz.payola.data.squeryl.entities.pipelines

import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl._

/**
 * This objects converts [[cz.payola.common.entities.pipelines.TransformerVisualizerBinding]]
 * to [[cz.payola.data.squeryl.entities.pipelines.TransformerVisualizerBinding]]
 */
object TransformerVisualizerBinding extends EntityConverter[TransformerVisualizerBinding]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[TransformerVisualizerBinding] = {
        entity match {
            case e: TransformerVisualizerBinding => Some(e)
            case e: cz.payola.common.entities.pipelines.TransformerVisualizerBinding => {
                val convertedBinding = new TransformerVisualizerBinding(e.id, Transformer(e.sourceTransformer),
                    Visualizer(e.targetVisualizer))
                Some(convertedBinding)
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.pipelines.TransformerVisualizerBinding]] entities.
 * @param id ID of this binding
 * @param s Source transformer of this binding
 * @param t Target visualizer of this binding
 * @param context Implicit context
 */
class TransformerVisualizerBinding(
    override val id: String,
    s: Transformer,
    t: Visualizer)(implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.pipelines.TransformerVisualizerBinding(s, t)
    with Entity
{
    val sourceTransformerId: String = Option(s).map(_.id).getOrElse(null)

    val targetVisualizerId: String = Option(t).map(_.id).getOrElse(null)

    var pipelineId: String = null

    def sourceTransformer_=(value: TransformerType) {
        _sourceTransformer = value
    }

    def targetVisualizer_=(value: VisualizerType) {
        _targetVisualizer = value
    }
}
*/