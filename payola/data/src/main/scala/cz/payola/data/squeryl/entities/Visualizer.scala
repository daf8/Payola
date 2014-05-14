package cz.payola.data.squeryl.entities

import scala.collection.immutable
import scala.collection.mutable
import cz.payola.data.squeryl._

/**
 * This object converts [[cz.payola.common.entities.Visualizer]] to [[cz.payola.data.squeryl.entities.Visualizer]]
 */
object Visualizer extends EntityConverter[Visualizer]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[Visualizer] = {
        entity match {
            case e: Visualizer => Some(e)
            case e: cz.payola.common.entities.Visualizer
            => Some(new Visualizer(e.id,e.name,e.owner.map(User(_)),e.isPublic))
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.Visualizer]] entities.
 * @param id ID of the Visualizer
 * @param name Name of the Visualizer
 * @param o Owner of the Visualizer
 * @param _isPub Whether the Visualizer is public or not
 * @param context Implicit context
 */
class Visualizer(override val id: String, name: String, o: Option[User], var _isPub: Boolean)
    (implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.Visualizer(name, o)
    with Entity with OptionallyOwnedEntity with ShareableEntity
{
    _compatibilityAnalysisChecks = null

    _compatibilityTransformerChecks = null

    private lazy val _compatibilityAnalysisCheckQuery = context.schema.visualizersToAnalysisCompatibilityChecks.left(this)

    private lazy val _compatibilityTransformerCheckQuery = context.schema.visualizersToTransformerCompatibilityChecks.left(this)

    override def compatibilityAnalysisChecks: immutable.Seq[VisualizerToAnalysisCompatibilityCheckType] = {
        if (_compatibilityAnalysisChecks == null) {
            context.visualizerRepository.loadCompatibilityAnalysisChecks(this)
        }

        _compatibilityAnalysisChecks.toList
    }

    def compatibilityAnalysisChecks_=(value: Seq[VisualizerToAnalysisCompatibilityCheckType]) {
        _compatibilityAnalysisChecks = mutable.ArrayBuffer(value: _*)
    }

    override def compatibilityTransformerChecks: immutable.Seq[VisualizerToTransformerCompatibilityCheckType] = {
        if (_compatibilityTransformerChecks == null) {
            context.visualizerRepository.loadCompatibilityTransformerChecks(this)
        }

        _compatibilityTransformerChecks.toList
    }

    def compatibilityTransformerChecks_=(value: Seq[VisualizerToTransformerCompatibilityCheckType]) {
        _compatibilityTransformerChecks = mutable.ArrayBuffer(value: _*)
    }

    override protected def storeAnalysisChecking(checking: Visualizer#VisualizerToAnalysisCompatibilityCheckType) {
        super.storeAnalysisChecking(associateAnalysisCompatibilityCheck(VisualizerToAnalysisCompatibilityCheck(checking)))
    }

    override protected def storeTransformerChecking(checking: Visualizer#VisualizerToTransformerCompatibilityCheckType) {
        super.storeTransformerChecking(associateTransformerCompatibilityCheck(VisualizerToTransformerCompatibilityCheck(checking)))
    }

    override protected def discardAnalysisChecking(checking: Visualizer#VisualizerToAnalysisCompatibilityCheckType) {
        context.visualizerRepository.removeCompatibilityAnalysisCheckById(checking.id)

        super.discardAnalysisChecking(checking)
    }

    override protected def discardTransformerChecking(checking: Visualizer#VisualizerToTransformerCompatibilityCheckType) {
        context.visualizerRepository.removeCompatibilityTransformerCheckById(checking.id)

        super.discardTransformerChecking(checking)
    }

    def associateAnalysisCompatibilityCheck(instance: VisualizerToAnalysisCompatibilityCheck): VisualizerToAnalysisCompatibilityCheck = {
        context.schema.associate(instance, _compatibilityAnalysisCheckQuery)
    }

    def associateTransformerCompatibilityCheck(instance: VisualizerToTransformerCompatibilityCheck): VisualizerToTransformerCompatibilityCheck = {
        context.schema.associate(instance, _compatibilityTransformerCheckQuery)
    }
}