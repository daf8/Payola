package cz.payola.domain.entities.transformers.optimization.plugins

import cz.payola.domain.entities.plugins._
import cz.payola.domain.entities.plugins.concrete._
import cz.payola.domain.entities.transformers.optimization.TransformerPluginWithInstance
import cz.payola.domain.entities.plugins.concrete.query.Limit

/**
  * An instance of the fetcher query optimization plugin.
  * @param sparqlQuery The SPARQL query plugin instance with its plugin.
  */
class LimitedQueryPluginInstance(val sparqlQuery: TransformerPluginWithInstance[SparqlQuery],
    val limit: TransformerPluginWithInstance[Limit])
    extends TransformerPluginInstance(new LimitedQueryPlugin, Nil)

