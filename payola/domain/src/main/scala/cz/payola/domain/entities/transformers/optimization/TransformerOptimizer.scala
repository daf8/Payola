package cz.payola.domain.entities.transformers.optimization

import cz.payola.domain.entities.Transformer

/**
 * Optimizer of analyses.
 * @param phases Phases of the optimization to perform.
 */
class TransformerOptimizer(val phases: Seq[TransformerOptimizationPhase])
{
    /**
     * Optimizes the specified transformer.
     * @param transformer The transformer to optimize.
     * @return An evaluationally-equivalent optimized transformer.
     */
    def optimize(transformer: Transformer): OptimizedTransformer = {
        phases.foldLeft[OptimizedTransformer](new OptimizedTransformer(transformer))((a, phase) => phase.run(a))
    }
}
