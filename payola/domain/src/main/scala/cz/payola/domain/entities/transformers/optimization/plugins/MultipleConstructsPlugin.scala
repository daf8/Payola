package cz.payola.domain.entities.transformers.optimization.plugins

import cz.payola.domain.entities.plugins._
import cz.payola.domain.entities.plugins.concrete.query.Construct
import cz.payola.domain.sparql._

/**
 * A plugin that during optimization replaces a sequence of construct plugins.
 */
object MultipleConstructsPlugin extends Construct("Merged SPARQL construct queries")
{
    def getConstructQuery(instance: PluginInstance, subject: Subject, variableGetter: () => Variable) = {
        instance match {
            case _ => throw new PluginException("This should not be called.")
        }
    }

    def transformerGetConstructQuery(instance: TransformerPluginInstance, subject: Subject, variableGetter: () => Variable) = {
        instance match {
            case constructsInstance: MultipleConstructsPluginInstance => {
                val constructs = constructsInstance.constructs
                val queries = constructs.map(c => c.plugin.transformerGetConstructQuery(c.instance, subject, variableGetter))
                queries.fold(ConstructQuery.empty)(_ + _)
            }
            case _ => throw new PluginException("The specified plugin instance doesn't correspond to the plugin.")
        }
    }
}
