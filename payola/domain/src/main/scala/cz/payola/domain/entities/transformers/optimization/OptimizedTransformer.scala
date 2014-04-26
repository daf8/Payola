package cz.payola.domain.entities.transformers.optimization

import collection.immutable
import collection.mutable
import cz.payola.domain.entities.Transformer
import cz.payola.domain.entities.plugins.TransformerPluginInstance

/**
 * An transformer that is target of optimizations (i.e. collapsing multiple plugin instances into one, or replacing
 * plugin instances with another). It tracks the current instances that replaced the original instances.
 * @param transformer
 */
class OptimizedTransformer(transformer: Transformer) extends Transformer(transformer.name, transformer.owner)
{
    /**
     * A map that for each plugin instance P contains a sequence of plugin instances that the instance P replaced
     * during the transformer optimization. If the instance P didn't replace any plugin, then the sequence contains only
     * the instance P.
     */
    val originalInstances: mutable.Map[TransformerPluginInstanceType, Seq[TransformerPluginInstanceType]] = transformer match {
        case optimizedTransformer: OptimizedTransformer => optimizedTransformer.originalInstances.clone()
        case _ => mutable.Map.empty
    }

    // Clone the plugin instances and bindings.
    transformer.pluginInstances.foreach(instance => addPluginInstance(instance))
    transformer.pluginInstanceBindings.foreach(binding => addBinding(binding))

    /**
     * Returns all the original instances.
     */
    def allOriginalInstances: immutable.Seq[TransformerPluginInstanceType] = {
        originalInstances.values.flatten.toList
    }

    /**
     * Replaces the specified old instances with the new instance, but doesn't take any care of the bindings.
     * It just removes the old instances from the transformer and adds the new one.
     * @param newInstance The new instance that should replace the old plugin instances.
     * @param oldInstances The plugin instances to replace.
     */
    def replaceInstances(newInstance: TransformerPluginInstanceType, oldInstances: TransformerPluginInstanceType*) {
        // Store the original instances before removing.
        val oldOriginalInstances = oldInstances.flatMap(i => originalInstances.getOrElse(i, Nil))

        // Update the transformer.
        removePluginInstances(oldInstances: _*)
        addPluginInstance(newInstance)

        // Update the original instances of the new instance.
        originalInstances.update(newInstance, oldOriginalInstances)
    }

    override def addPluginInstance(instance: TransformerPluginInstanceType) {
        super.addPluginInstance(instance)
        originalInstances += (instance -> List(instance))
    }

    override def removePluginInstance(instance: TransformerPluginInstanceType): Option[TransformerPluginInstance] = {
        originalInstances -= instance
        super.removePluginInstance(instance)
    }

    override def collapseBinding(binding: TransformerPluginInstanceBindingType, instance: TransformerPluginInstance) {
        // Store the original instances before they get removed within the collapse binding super call.
        val sourceOriginalInstances = originalInstances.getOrElse(binding.sourcePluginInstance, Nil)
        val targetOriginalInstances = originalInstances.getOrElse(binding.targetPluginInstance, Nil)

        super.collapseBinding(binding, instance)

        // Update the original instances of the collapsed instance.
        originalInstances.update(instance, sourceOriginalInstances ++ targetOriginalInstances)
    }
}
