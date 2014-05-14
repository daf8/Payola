package cz.payola.domain.entities

import scala.collection.mutable
import plugins._
import cz.payola.domain.Entity
import cz.payola.domain.entities.settings.OntologyCustomization
import cz.payola.domain.entities.plugins.concrete._
import parameters._
import scala.Some
import scala.Some

/**
 * @param _name Name of the visualizer.
 */
class Visualizer(protected var _name: String, protected var _owner: Option[User])
    extends Entity
    with NamedEntity
    with OptionallyOwnedEntity
    with cz.payola.common.entities.Visualizer
{
    checkConstructorPostConditions()

    type VisualizerToAnalysisCompatibilityCheckType = VisualizerToAnalysisCompatibilityCheck

    type VisualizerToTransformerCompatibilityCheckType = VisualizerToTransformerCompatibilityCheck

    def addAnalysisChecking(checking: VisualizerToAnalysisCompatibilityCheckType) {
        storeAnalysisChecking(checking)
    }

    def addAnalysisChecking(visualizer:String, compatibleAnalysis: Analysis) {
        addAnalysisChecking(new VisualizerToAnalysisCompatibilityCheck(visualizer, compatibleAnalysis))
    }

    def addTransformerChecking(checking: VisualizerToTransformerCompatibilityCheckType) {
        storeTransformerChecking(checking)
    }

    def addTransformerChecking(visualizer: String, compatibleTransformer: Transformer) {
        addTransformerChecking(new VisualizerToTransformerCompatibilityCheck(visualizer, compatibleTransformer))
    }

    def removeAnalysisChecking(checking: VisualizerToAnalysisCompatibilityCheckType): Option[VisualizerToAnalysisCompatibilityCheckType] = {
        ifContains(compatibilityAnalysisChecks, checking) {
            discardAnalysisChecking(checking)
        }
    }

    def removeTransformerChecking(checking: VisualizerToTransformerCompatibilityCheckType): Option[VisualizerToTransformerCompatibilityCheckType] = {
        ifContains(compatibilityTransformerChecks, checking) {
            discardTransformerChecking(checking)
        }
    }
}