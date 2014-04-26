package cz.payola.domain.entities.transformers.optimization.phases

import cz.payola.domain.entities.transformers.optimization.plugins._
import cz.payola.domain.entities.plugins.concrete._
import cz.payola.domain.entities.transformers.optimization.TransformerPluginWithInstance
import cz.payola.domain.entities.transformers.optimization._

/**
 * Merges data fetcher plugin instances with SPARQL queries.
 */
class TransformerMergeFetchersWithQueries extends TransformerOptimizationPhase
{
    def run(transformer: OptimizedTransformer): OptimizedTransformer = {
        transformer.pluginInstanceBindings.foreach { binding =>
            val source = binding.sourcePluginInstance
            val target = binding.targetPluginInstance

            source.plugin match {
                case dataFetcher: DataFetcher => target.plugin match {
                    case sparqlQuery: SparqlQuery => {
                        transformer.collapseBinding(binding, new FetcherQueryPluginInstance(
                            TransformerPluginWithInstance(dataFetcher, source), TransformerPluginWithInstance(sparqlQuery, target)))
                    }
                    case _ => target match { // added by Jiri Helmich, handle already merged Query with Limit plugin
                        case limitedQuery: LimitedQueryPluginInstance => {
                            transformer.collapseBinding(binding, new FetcherLimitedQueryPluginInstance(
                                TransformerPluginWithInstance(dataFetcher, source), TransformerPluginWithInstance(new LimitedQueryPlugin, target)
                            ))
                        }
                        case _ =>
                    }
                }
                case _ =>
            }
        }

        transformer
    }
}
