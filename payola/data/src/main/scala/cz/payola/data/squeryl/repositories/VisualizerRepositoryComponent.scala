package cz.payola.data.squeryl.repositories

import cz.payola.data.squeryl._
import cz.payola.data.squeryl.entities.transformers._
import org.squeryl.PrimitiveTypeMode._
import cz.payola.data.squeryl.entities._
import cz.payola.data.squeryl.entities.plugins._
import cz.payola.domain.entities.settings._
import scala.Some

/**
 * Provides repository to access persisted transformers
 */
trait VisualizerRepositoryComponent extends TableRepositoryComponent
{
    self: SquerylDataContextComponent =>

    private lazy val compatibilityAnalysisCheckRepository = new LazyTableRepository[VisualizerToAnalysisCompatibilityCheck](
        schema.visualizerToAnalysisCompatibilityChecks, VisualizerToAnalysisCompatibilityCheck)

    private lazy val compatibilityTransformerCheckRepository = new LazyTableRepository[VisualizerToTransformerCompatibilityCheck](
        schema.visualizerToTransformerCompatibilityChecks, VisualizerToTransformerCompatibilityCheck)

    /**
     * A repository to access persisted transformers
     */
    lazy val visualizerRepository = new VisualizerDefaultTableRepository

    class VisualizerDefaultTableRepository
        extends OptionallyOwnedEntityDefaultTableRepository[Visualizer](schema.visualizers, Visualizer)
        with NamedEntityTableRepository[Visualizer]
        with ShareableEntityTableRepository[Visualizer, (Visualizer, Option[User])]
        with VisualizerRepository
    {
        override def persist(entity: AnyRef): Visualizer = wrapInTransaction {
            val visualizer = super.persist(entity)

            // Associate plugin instances with their bindings and default customization
            entity match {
                case a: Visualizer => // Everything already persisted
                case a: cz.payola.domain.entities.Visualizer => {
                    a.compatibilityAnalysisChecks.map(c => visualizer.associateAnalysisCompatibilityCheck(VisualizerToAnalysisCompatibilityCheck(c)))
                    a.compatibilityTransformerChecks.map(d => visualizer.associateTransformerCompatibilityCheck(VisualizerToTransformerCompatibilityCheck(d)))
                }
            }

            visualizer
        }

        def removeCompatibilityAnalysisCheckById(compatibilityCheckId: String): Boolean = wrapInTransaction {
            compatibilityAnalysisCheckRepository.removeById(compatibilityCheckId)
        }

        def removeCompatibilityTransformerCheckById(compatibilityCheckId: String): Boolean = wrapInTransaction {
            compatibilityTransformerCheckRepository.removeById(compatibilityCheckId)
        }

        def loadCompatibilityAnalysisChecks(visualizer: Visualizer) {
            _loadVisualizer(visualizer)
        }

        def loadCompatibilityTransformerChecks(visualizer: Visualizer) {
            _loadVisualizer(visualizer)
        }

        private def _loadVisualizer(visualizer: Visualizer) {
            wrapInTransaction {
                val compatibilityAnalysisChecks = compatibilityAnalysisCheckRepository.selectWhere(c => c.visualizerId === visualizer.id)
                val compatibilityTransformerChecks = compatibilityTransformerCheckRepository.selectWhere(d => d.visualizerId === visualizer.id)

                compatibilityAnalysisChecks.foreach {c =>
                    c.compatibleAnalysis = analysisRepository.getById(c.compatibleAnalysisId).get
                }

                compatibilityTransformerChecks.foreach {d =>
                    d.compatibleTransformer = transformerRepository.getById(d.compatibleTransformerId).get
                }

                visualizer.compatibilityAnalysisChecks = compatibilityAnalysisChecks
                visualizer.compatibilityTransformerChecks = compatibilityTransformerChecks
            }
        }

    }

}