package cz.payola.common.entities

import scala.collection._
import cz.payola.common.Entity

trait Visualizer extends Entity with NamedEntity with OptionallyOwnedEntity with ShareableEntity
{
    /** Type of the compatibility check between plugin instance and analysis. */
    type VisualizerToAnalysisCompatibilityCheckType <: VisualizerToAnalysisCompatibilityCheck

    /** Type of the compatibility check between plugin instance and transformer. */
    type VisualizerToTransformerCompatibilityCheckType <: VisualizerToTransformerCompatibilityCheck

    protected var _compatibilityAnalysisChecks = mutable.ArrayBuffer[VisualizerToAnalysisCompatibilityCheckType]()

    protected var _compatibilityTransformerChecks = mutable.ArrayBuffer[VisualizerToTransformerCompatibilityCheckType]()


    /** Bindings between the analytical plugin instances. */
    def compatibilityAnalysisChecks: immutable.Seq[VisualizerToAnalysisCompatibilityCheckType] = _compatibilityAnalysisChecks.toList

    /** Bindings between the analytical plugin instances. */
    def compatibilityTransformerChecks: immutable.Seq[VisualizerToTransformerCompatibilityCheckType] = _compatibilityTransformerChecks.toList

    /**
     * Stores the specified check to the transformer.
     * @param checking Plugin instance compatibility with data source
     */
    protected def storeAnalysisChecking(checking: VisualizerToAnalysisCompatibilityCheckType) {
        //_compatibilityAnalysisChecks += checking
    }

    /**
     * Stores the specified check to the transformer.
     * @param checking Plugin instance compatibility with data source
     */
    protected def storeTransformerChecking(checking: VisualizerToTransformerCompatibilityCheckType) {
        //_compatibilityTransformerChecks += checking
    }

    /**
     * Discards the specified compatibility check from the transformer. Complementary operation to store.
     * @param checking The compatibility check to discard.
     */
    protected def discardAnalysisChecking(checking: VisualizerToAnalysisCompatibilityCheckType) {
        //_compatibilityAnalysisChecks -= checking
    }

    /**
     * Discards the specified compatibility check from the transformer. Complementary operation to store.
     * @param checking The compatibility check to discard.
     */
    protected def discardTransformerChecking(checking: VisualizerToTransformerCompatibilityCheckType) {
        //_compatibilityTransformerChecks -= checking
    }
}