package cz.payola.data.squeryl.entities.analyses

import cz.payola.common.entities._
import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl.entities.plugins.DataSource
import cz.payola.data.squeryl.entities.plugins.PluginInstance
import cz.payola._

/**
 * This objects converts [[analyses.CompatibilityCheck]]
 * to [[entities.analyses.CompatibilityCheck]]
 */
object CompatibilityCheck extends EntityConverter[CompatibilityCheck]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[CompatibilityCheck] = {
        entity match {
            case e: CompatibilityCheck => Some(e)
            case e: cz.payola.common.entities.analyses.CompatibilityCheck => {
                val convertedBinding = new CompatibilityCheck(e.id, PluginInstance(e.sourcePluginInstance),
                    DataSource(e.compatibleDataSource))
                Some(convertedBinding)
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[domain.entities.analyses.CompatibilityCheck]] entities.
 * @param id ID of this binding
 * @param s Source plugin instance of this binding
 * @param t Target data source of this binding
 * @param context Implicit context
 */
class CompatibilityCheck(
    override val id: String,
    s: PluginInstance,
    t: DataSource)(implicit val context: SquerylDataContextComponent)
    extends domain.entities.analyses.CompatibilityCheck(s, t)
    with Entity
{
    val sourcePluginInstanceId: String = Option(s).map(_.id).getOrElse(null)

    val compatibleDataSourceId: String = Option(t).map(_.id).getOrElse(null)

    var analysisId: String = null

    def sourcePluginInstance_=(value: PluginInstanceType) {
        _sourcePluginInstance = value
    }

    def compatibleDataSource_=(value: DataSourceType) {
        _compatibleDataSource = value
    }
}
