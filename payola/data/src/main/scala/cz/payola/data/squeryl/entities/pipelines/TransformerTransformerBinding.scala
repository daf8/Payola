/*package cz.payola.data.squeryl.entities.pipelines

import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl._

/**
 * This objects converts [[cz.payola.common.entities.pipelines.TransformerTransformerBinding]]
 * to [[cz.payola.data.squeryl.entities.pipelines.TransformerTransformerBinding]]
 */
object TransformerTransformerBinding extends EntityConverter[TransformerTransformerBinding]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[TransformerTransformerBinding] = {
        entity match {
            case e: TransformerTransformerBinding => Some(e)
            case e: cz.payola.common.entities.pipelines.TransformerTransformerBinding => {
                val convertedBinding = new TransformerTransformerBinding(e.id, Transformer(e.sourceTransformer),
                    Transformer(e.targetTransformer))
                Some(convertedBinding)
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.pipelines.TransformerTransformerBinding]] entities.
 * @param id ID of this binding
 * @param s Source transformer of this binding
 * @param t Target transformer of this binding
 * @param context Implicit context
 */
class TransformerTransformerBinding(
    override val id: String,
    s: Transformer,
    t: Transformer)(implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.pipelines.TransformerTransformerBinding(s, t)
    with Entity
{
    val sourceTransformerId: String = Option(s).map(_.id).getOrElse(null)

    val targetTransformerId: String = Option(t).map(_.id).getOrElse(null)

    var pipelineId: String = null

    def sourceTransformer_=(value: TransformerType) {
        _sourceTransformer = value
    }

    def targetTransformer_=(value: TransformerType) {
        _targetTransformer = value
    }
}
*/