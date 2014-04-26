package cz.payola.domain.entities.transformers.optimization.phases

import cz.payola.domain.entities.transformers.optimization.plugins._
import cz.payola.domain.entities.plugins.TransformerPluginInstance
import cz.payola.domain.entities.plugins.concrete.query.Construct
import cz.payola.domain.entities.transformers.optimization._

/**
 * Merges multiple construct plugin instances into one.
 */
class TransformerMergeConstructs extends TransformerOptimizationPhase
{
    def run(transformer: OptimizedTransformer): OptimizedTransformer = {
        merge(transformer, transformer.outputInstance.get)
        transformer
    }

    /**
     * Merge the specified plugin instance with preceding instance in case it's possible. Merges all the preceding
     * instances with their predecessors recursively.
     * @param transformer The transformer where the merge is performed.
     * @param instance The instance to merge.
     */
    def merge(transformer: OptimizedTransformer, instance: TransformerPluginInstance) {
        val inputBindings = transformer.pluginInstanceInputBindings(instance)
        inputBindings.headOption.foreach { binding =>
            val source = binding.sourcePluginInstance
            val target = binding.targetPluginInstance
            if (source.plugin.isInstanceOf[Construct] && target.plugin.isInstanceOf[Construct]) {
                val collapsedInstance = target match {
                    case targetConstructs: MultipleConstructsPluginInstance => targetConstructs + source
                    case _ => MultipleConstructsPluginInstance(target, source)
                }
                transformer.collapseBinding(binding, collapsedInstance)
                merge(transformer, collapsedInstance)
            } else {
                inputBindings.foreach(binding => merge(transformer, binding.sourcePluginInstance))
            }
        }
    }
}
