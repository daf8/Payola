package cz.payola.domain.entities.transformers.optimization.plugins

import cz.payola.domain.entities.plugins._
import cz.payola.domain.entities.plugins.concrete._
import cz.payola.domain.entities.transformers.optimization.plugins._
import cz.payola.domain.entities.transformers.optimization.TransformerPluginWithInstance

/**
  * An instance of the fetcher query optimization plugin.
  * @param dataFetcher The data fetcher plugin instance with its plugin.
  * @param sparqlQuery The SPARQL query plugin instance with its plugin.
  */
class FetcherQueryPluginInstance(val dataFetcher: TransformerPluginWithInstance[DataFetcher],
    val sparqlQuery: TransformerPluginWithInstance[SparqlQuery])
    extends TransformerPluginInstance(FetcherQueryPlugin, Nil)

class FetcherLimitedQueryPluginInstance(val dataFetcher: TransformerPluginWithInstance[DataFetcher],
    val limitedSparqlQuery: TransformerPluginWithInstance[LimitedQueryPlugin])
    extends TransformerPluginInstance(FetcherQueryPlugin, Nil)

