package cz.payola.domain.entities.plugins.concrete.data

import scala.collection.immutable
import scala.collection.JavaConversions._
import cz.payola.domain.IDGenerator
import cz.payola.domain.entities.plugins._
import cz.payola.domain.entities.plugins.concrete.DataFetcher
import cz.payola.domain.entities.plugins.parameters.StringParameter
import cz.payola.domain.rdf._
import com.hp.hpl.jena.query.QueryFactory

sealed class Input(name: String, inputCount: Int, parameters: immutable.Seq[Parameter[_]], id: String)
    extends DataFetcher(name, inputCount, parameters, id)
{
    def this() = {
        this("Input", 0, List(
            new StringParameter(Input.nameParameter, "", false, false, false, true),
            new StringParameter(Input.descParameter, "", true, false, false, true),
            new StringParameter(Input.askQueryParameter, "", true, false, false, true)
        ), IDGenerator.newId)
    }

    def getName(instance: PluginInstance): Option[String] = {
        instance.getStringParameter(Input.nameParameter)
    }

    def getDesc(instance: PluginInstance): Option[String] = {
        instance.getStringParameter(Input.descParameter)
    }

    def getAsk(instance: PluginInstance): Option[String] = {
        instance.getStringParameter(Input.askQueryParameter)
    }

    def executeQuery(instance: PluginInstance, query: String): Graph = {
        /*usingDefined(getEndpointURL(instance), getGraphURIs(instance)) { (endpointURL, endpointGraphURIs) =>
            val sparqlQuery = QueryFactory.create(query)
            val queryGraphURIs = sparqlQuery.getGraphURIs.toList

            // Replace the graph URIs with intersection of them and URIs specified in the endpoint. If any of the two
            // collections is empty, then it represents all graphs.
            val union = endpointGraphURIs.union(queryGraphURIs)
            val intersection = endpointGraphURIs.intersect(queryGraphURIs)
            val graphURIs =
                if (endpointGraphURIs.isEmpty || queryGraphURIs.isEmpty) {
                    Some(union)
                } else if (intersection.nonEmpty) {
                    Some(intersection)
                } else {
                    None
                }

            // Execute the query only if the intersection wasn't empty.
            val result = graphURIs.map { uris =>
                sparqlQuery.getGraphURIs.clear()
                uris.foreach(sparqlQuery.addGraphURI(_))
                new SparqlEndpoint(endpointURL).executeQuery(sparqlQuery.toString)
            }

            result.getOrElse(*/JenaGraph.empty/*)
        }*/
    }

    def askQuery(instance: PluginInstance): Boolean = {
        /*usingDefined(getEndpointURL(instance), getAsk(instance)) { (endpointURL, query) =>
            val sparqlQuery = QueryFactory.create(query)
            val result: String = new SparqlEndpoint(endpointURL).askQuery(sparqlQuery.toString)
            result.toBoolean
        }*/
        true
    }

    def askQuerySource(instance: PluginInstance, query: String): Boolean = false
}

object Input
{
    val nameParameter = "Name"

    val descParameter = "Description"

    val askQueryParameter = "ASK query"
}
