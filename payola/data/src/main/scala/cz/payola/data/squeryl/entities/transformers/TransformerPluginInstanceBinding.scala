package cz.payola.data.squeryl.entities.transformers

import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl.entities.plugins.TransformerPluginInstance
import cz.payola.data.squeryl._

/**
 * This objects converts [[cz.payola.common.entities.transformers.TransformerPluginInstanceBinding]]
 * to [[cz.payola.data.squeryl.entities.transformers.TransformerPluginInstanceBinding]]
 */
object TransformerPluginInstanceBinding extends EntityConverter[TransformerPluginInstanceBinding]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[TransformerPluginInstanceBinding] = {
        entity match {
            case e: TransformerPluginInstanceBinding => Some(e)
            case e: cz.payola.common.entities.transformers.TransformerPluginInstanceBinding => {
                val convertedBinding = new TransformerPluginInstanceBinding(e.id, TransformerPluginInstance(e.sourcePluginInstance),
                    TransformerPluginInstance(e.targetPluginInstance), e.targetInputIndex)
                Some(convertedBinding)
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.transformers.TransformerPluginInstanceBinding]] entities.
 * @param id ID of this binding
 * @param s Source plugin instance of this binding
 * @param t Target plugin instance of this binding
 * @param idx Input index of this binding
 * @param context Implicit context
 */
class TransformerPluginInstanceBinding(
    override val id: String,
    s: TransformerPluginInstance,
    t: TransformerPluginInstance,
    idx: Int = 0)(implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.transformers.TransformerPluginInstanceBinding(s, t, idx)
    with Entity
{
    val sourcePluginInstanceId: String = Option(s).map(_.id).getOrElse(null)

    val targetPluginInstanceId: String = Option(t).map(_.id).getOrElse(null)

    val inputIndex = idx

    var transformerId: String = null

    def sourcePluginInstance_=(value: TransformerPluginInstanceType) {
        _sourcePluginInstance = value
    }

    def targetPluginInstance_=(value: TransformerPluginInstanceType) {
        _targetPluginInstance = value
    }
}
