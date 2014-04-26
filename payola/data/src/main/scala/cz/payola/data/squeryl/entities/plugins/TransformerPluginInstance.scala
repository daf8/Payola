package cz.payola.data.squeryl.entities.plugins

import scala.collection.immutable
import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl._

/**
 * This object converts [[cz.payola.common.entities.plugins.TransformerPluginInstance]] to [[cz.payola.data.squeryl.entities
 * .plugins.TransformerPluginInstance]]
 */
object TransformerPluginInstance extends EntityConverter[TransformerPluginInstance]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[TransformerPluginInstance] = {
        entity match {
            case e: TransformerPluginInstance => Some(e)
            case e: cz.payola.common.entities.plugins.TransformerPluginInstance => {
                val plugin = e.plugin.asInstanceOf[cz.payola.domain.entities.Plugin]
                Some(new TransformerPluginInstance(e.id, plugin, e.parameterValues.map(ParameterValue(_)), e.description,
                    e.isEditable))
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.plugins.PluginInstance]] entities.
 * @param id ID of the plugin instance
 * @param p Plugin the instance is derived from
 * @param paramValues List of parameter values for the plugin instance
 * @param _desc Description
 * @param _isEdit Whether the plugin instance is editable or not
 * @param context Implicit context
 */
class TransformerPluginInstance(
    override val id: String,
    p: cz.payola.domain.entities.Plugin,
    paramValues: immutable.Seq[ParameterValue[_]],
    var _desc: String,
    var _isEdit: Boolean)(implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.plugins.TransformerPluginInstance(p, paramValues)
    with Entity with DescribedEntity with TransformerPluginInstanceLike
{
    var pluginId: String = Option(p).map(_.id).getOrElse(null)

    var transformerId: String = null
}
