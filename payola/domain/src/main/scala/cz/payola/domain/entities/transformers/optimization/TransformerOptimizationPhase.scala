package cz.payola.domain.entities.transformers.optimization

/**
 * A phase of transformer optimization.
 */
trait TransformerOptimizationPhase
{
    /**
     * Runs the optimization phase on the specified transformer.
     * @param transformer The transformer to run the optimization phase on.
     * @return The transformer optimized using the phase.
     */
    def run(transformer: OptimizedTransformer): OptimizedTransformer
}
