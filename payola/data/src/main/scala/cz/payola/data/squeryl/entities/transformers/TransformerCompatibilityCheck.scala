package cz.payola.data.squeryl.entities.transformers

import cz.payola.common.entities._
import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl.entities.plugins.DataSource
import cz.payola.data.squeryl.entities.plugins.TransformerPluginInstance
import cz.payola._

/**
 * This objects converts [[transformers.TransformerCompatibilityCheck]]
 * to [[entities.transformers.TransformerCompatibilityCheck]]
 */
object TransformerCompatibilityCheck extends EntityConverter[TransformerCompatibilityCheck]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[TransformerCompatibilityCheck] = {
        entity match {
            case e: TransformerCompatibilityCheck => Some(e)
            case e: cz.payola.common.entities.transformers.TransformerCompatibilityCheck => {
                val convertedBinding = new TransformerCompatibilityCheck(e.id, TransformerPluginInstance(e.sourcePluginInstance),
                    DataSource(e.compatibleDataSource))
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
class TransformerCompatibilityCheck(
    override val id: String,
    s: TransformerPluginInstance,
    t: DataSource)(implicit val context: SquerylDataContextComponent)
    extends domain.entities.transformers.TransformerCompatibilityCheck(s, t)
    with Entity
{
    val sourcePluginInstanceId: String = Option(s).map(_.id).getOrElse(null)

    val compatibleDataSourceId: String = Option(t).map(_.id).getOrElse(null)

    var transformerId: String = null

    def sourcePluginInstance_=(value: TransformerPluginInstanceType) {
        _sourcePluginInstance = value
    }

    def compatibleDataSource_=(value: DataSourceType) {
        _compatibleDataSource = value
    }
}
