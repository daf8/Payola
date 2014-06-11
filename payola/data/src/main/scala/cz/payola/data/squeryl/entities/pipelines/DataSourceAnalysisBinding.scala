/*package cz.payola.data.squeryl.entities.pipelines

import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities.plugins.DataSource

/**
 * This objects converts [[cz.payola.common.entities.pipelines.DataSourceAnalysisBinding]]
 * to [[cz.payola.data.squeryl.entities.pipelines.DataSourceAnalysisBinding]]
 */
object DataSourceAnalysisBinding extends EntityConverter[DataSourceAnalysisBinding]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[DataSourceAnalysisBinding] = {
        entity match {
            case e: DataSourceAnalysisBinding => Some(e)
            case e: cz.payola.common.entities.pipelines.DataSourceAnalysisBinding => {
                val convertedBinding = new DataSourceAnalysisBinding(e.id, DataSource(e.sourceDataSource),
                    Analysis(e.targetAnalysis))
                Some(convertedBinding)
            }
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.pipelines.DataSourceAnalysisBinding]] entities.
 * @param id ID of this binding
 * @param s Source data source of this binding
 * @param t Target analysis of this binding
 * @param context Implicit context
 */
class DataSourceAnalysisBinding(
    override val id: String,
    s: DataSource,
    t: Analysis)(implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.pipelines.DataSourceAnalysisBinding(s, t)
    with Entity
{
    val sourceDataSourceId: String = Option(s).map(_.id).getOrElse(null)

    val targetAnalysisId: String = Option(t).map(_.id).getOrElse(null)

    var pipelineId: String = null

    def sourceDataSource_=(value: DataSourceType) {
        _sourceDataSource = value
    }

    def targetAnalysis_=(value: AnalysisType) {
        _targetAnalysis = value
    }
}
*/