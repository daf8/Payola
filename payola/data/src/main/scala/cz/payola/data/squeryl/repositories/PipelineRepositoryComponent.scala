package cz.payola.data.squeryl.repositories

import cz.payola.data.squeryl._
//import cz.payola.data.squeryl.entities.pipelines._
import org.squeryl.PrimitiveTypeMode._
import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl.entities.plugins._
import cz.payola.domain.entities.settings._
import scala.Some

/**
 * Provides repository to access persisted analyses
 */
trait PipelineRepositoryComponent extends TableRepositoryComponent
{
    self: SquerylDataContextComponent =>
    /*private lazy val dataSourceAnalysisBindingRepository = new LazyTableRepository[DataSourceAnalysisBinding](
        schema.dataSourceAnalysisBindings, DataSourceAnalysisBinding)

    private lazy val analysisTransformerBindingRepository = new LazyTableRepository[AnalysisTransformerBinding](
        schema.analysisTransformerBindings, AnalysisTransformerBinding)

    private lazy val analysisVisualizerBindingRepository = new LazyTableRepository[AnalysisVisualizerBinding](
        schema.analysisVisualizerBindings, AnalysisVisualizerBinding)

    private lazy val transformerTransformerBindingRepository = new LazyTableRepository[TransformerTransformerBinding](
        schema.transformerTransformerBindings, TransformerTransformerBinding)

    private lazy val transformerVisualizerBindingRepository = new LazyTableRepository[TransformerVisualizerBinding](
        schema.transformerVisualizerBindings, TransformerVisualizerBinding)*/

    /**
     * A repository to access persisted analyses
     */
    lazy val pipelineRepository = new PipelineDefaultTableRepository

    class PipelineDefaultTableRepository
        extends OptionallyOwnedEntityDefaultTableRepository[Pipeline](schema.pipelines, Pipeline)
        with PipelineRepository
        with NamedEntityTableRepository[Pipeline]
        with ShareableEntityTableRepository[Pipeline, (Pipeline, Option[User])]
    {
        override def persist(entity: AnyRef): Pipeline = wrapInTransaction {
            val pipeline = super.persist(entity)

            // Associate plugin instances with their bindings and default customization
            entity match {
                case a: Pipeline => // Everything already persisted
                case a: cz.payola.domain.entities.Pipeline => {
                    //a.dataSources.map(ds => pipeline.associateDataSource(DataSource(ds)))
                    //a.dataSourceAnalysisBindings.map(dsab => pipeline.associateDataSourceAnalysisBinding(DataSourceAnalysisBinding(dsab)))
                    //a.analysis.map(a => pipeline.associateAnalysis(Analysis(a)))
                    //a.analysisTransformerBinding.map(atb => pipeline.associateAnalysisTransformerBinding(AnalysisTransformerBinding(atb)))
                    //a.analysisVisualizerBinding.map(avb => pipeline.associateAnalysisVisualizerBinding(AnalysisVisualizerBinding(avb)))
                    //a.transformers.map(t => pipeline.associateTransformer(Transformer(t)))
                    //a.transformerTransformerBindings.map(ttb => pipeline.associateTransformerTransformerBinding(TransformerTransformerBinding(ttb)))
                    //a.transformerVisualizerBinding.map(tvb => pipeline.associateTransformerVisualizerBinding(TransformerVisualizerBinding(tvb)))
                    //a.visualizer.map(v => pipeline.associateVisualizer(Visualizer(v)))
                }
            }

            pipeline
        }
        /* TODO
        def removePluginInstanceById(pluginInstanceId: String): Boolean = wrapInTransaction {
            schema.pluginInstances.deleteWhere(e => pluginInstanceId === e.id) == 1
        }

        def removePluginInstanceBindingById(pluginInstanceBindingId: String): Boolean = wrapInTransaction {
            pluginInstanceBindingRepository.removeById(pluginInstanceBindingId)
        }

        def removeCompatibilityCheckById(compatibilityCheckId: String): Boolean = wrapInTransaction {
            compatibilityCheckRepository.removeById(compatibilityCheckId)
        }
*/
        def loadDataSources(pipeline: Pipeline) {
            _loadPipeline(pipeline)
        }
/*
        def loadPluginInstanceBindings(analysis: Analysis) {
            _loadAnalysis(analysis)
        }

        def loadCompatibilityChecks(analysis: Analysis) {
            _loadAnalysis(analysis)
        }
*/
        private def _loadPipeline(pipeline: Pipeline) {
            wrapInTransaction {
                //val pluginInstancesByIds =
                //    loadPluginInstancesByFilter(pi => pi.asInstanceOf[PluginInstance].analysisId === analysis.id)
                //        .map(p => (p.id, p.asInstanceOf[PluginInstance])).toMap
                //val instanceBindings = pluginInstanceBindingRepository.selectWhere(b => b.analysisId === analysis.id)
                //val compatibilityChecks = compatibilityCheckRepository.selectWhere(c => c.analysisId === analysis.id)

                //val dataSources = dataSourceRepository.selectWhere(d => d.pipelineId === pipeline.id)


                // Set plugin instances to bindings
                /*instanceBindings.foreach {b =>
                    b.sourcePluginInstance = pluginInstancesByIds(b.sourcePluginInstanceId)
                    b.targetPluginInstance = pluginInstancesByIds(b.targetPluginInstanceId)
                }

                compatibilityChecks.foreach {c =>
                    c.sourcePluginInstance = pluginInstancesByIds(c.sourcePluginInstanceId)
                    c.compatibleDataSource = dataSourceRepository.getById(c.compatibleDataSourceId).get
                }*/

                //pipeline.dataSources = dataSources
                //analysis.pluginInstances = pluginInstancesByIds.values.toSeq
                //analysis.pluginInstanceBindings = instanceBindings
                //analysis.compatibilityChecks = compatibilityChecks
                //analysis.defaultOntologyCustomization = _getDefaultOntologyCustomization(analysis.defaultCustomizationId)
            }
        }
    }
}