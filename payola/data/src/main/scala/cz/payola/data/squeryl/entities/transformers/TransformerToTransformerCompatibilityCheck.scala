package cz.payola.data.squeryl.entities.transformers

//import cz.payola.common.entities._
import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl.entities.plugins.TransformerPluginInstance
import cz.payola._

/**
 * This objects converts [[transformers.TransformerToTransformerCompatibilityCheck]]
 * to [[entities.transformers.TransformerToTransformerCompatibilityCheck]]
 */
object TransformerToTransformerCompatibilityCheck extends EntityConverter[TransformerToTransformerCompatibilityCheck]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[TransformerToTransformerCompatibilityCheck] = {
        entity match {
            case e: TransformerToTransformerCompatibilityCheck => Some(e)
            case e: cz.payola.common.entities.transformers.TransformerToTransformerCompatibilityCheck => {
                val convertedBinding = new TransformerToTransformerCompatibilityCheck(e.id, TransformerPluginInstance(e.sourcePluginInstance),
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
 * @param s Source plugin instance of this binding
 * @param t Target data source of this binding
 * @param context Implicit context
 */
class TransformerToTransformerCompatibilityCheck(
    override val id: String,
    s: TransformerPluginInstance,
    t: Transformer
    )(implicit val context: SquerylDataContextComponent)
    extends domain.entities.transformers.TransformerToTransformerCompatibilityCheck(s, t)
    with Entity
{
    val sourcePluginInstanceId: String = Option(s).map(_.id).getOrElse(null)

    val compatibleTransformerId: String = Option(t).map(_.id).getOrElse(null)

    var transformerId: String = null

    def sourcePluginInstance_=(value: TransformerPluginInstanceType) {
        _sourcePluginInstance = value
    }

    def compatibleTransformer_=(value: TransformerType) {
        _compatibleTransformer = value
    }
}
