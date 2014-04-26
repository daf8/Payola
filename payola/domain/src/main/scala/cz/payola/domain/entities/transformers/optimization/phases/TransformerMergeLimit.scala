package cz.payola.domain.entities.transformers.optimization.phases

import cz.payola.domain.entities.transformers.optimization.plugins._
import cz.payola.domain.entities.plugins.concrete._
import cz.payola.domain.entities.plugins.concrete.query._
import cz.payola.domain.entities.transformers.optimization.TransformerPluginWithInstance
import cz.payola.domain.entities.transformers.optimization._

/**
 * Handle optimalizations of Limit plugin.
 *
 * Merges data fetcher plugin instances with SPARQL queries.
 * @author Jiri Helmich
 */
class TransformerMergeLimit extends TransformerOptimizationPhase
{
    def run(transformer: OptimizedTransformer): OptimizedTransformer = {
        transformer.pluginInstanceBindings.foreach { binding =>
            val source = binding.sourcePluginInstance
            val target = binding.targetPluginInstance

            source.plugin match {
                case c: Construct => //will merge in another phase
                case q: SparqlQuery if !q.transformerGetQuery(source).toUpperCase.contains("LIMIT") => target.plugin match {
                    case limit: Limit => {
                        transformer.collapseBinding(binding, new LimitedQueryPluginInstance(TransformerPluginWithInstance(q, source), TransformerPluginWithInstance(limit, target)))
                    }
                    case _ =>
                }
                case _ =>
            }
        }

        transformer
    }
}
