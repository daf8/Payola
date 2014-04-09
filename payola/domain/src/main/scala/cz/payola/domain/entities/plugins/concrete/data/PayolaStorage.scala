package cz.payola.domain.entities.plugins.concrete.data

import scala.collection.immutable
import cz.payola.domain._
import cz.payola.domain.entities.plugins._
import cz.payola.domain.entities.plugins.concrete.DataFetcher
import cz.payola.domain.entities.plugins.parameters.StringParameter
import cz.payola.domain.rdf._
import com.hp.hpl.jena.query.QueryFactory

sealed class PayolaStorage(name: String, inputCount: Int, parameters: immutable.Seq[Parameter[_]], id: String)
    (implicit val storageComponent: RdfStorageComponent)
    extends DataFetcher(name, inputCount, parameters, id)
{
    def this() = {
        this(PayolaStorage.pluginName, 0, List(
            new StringParameter(PayolaStorage.groupURIParameter, "", false, false, false, true),
            new StringParameter(PayolaStorage.askQueryParameter, "", true, false, false, true)
        ), IDGenerator.newId)(null)
        isPublic = false
    }

    def getAsk(instance: PluginInstance): Option[String] = {
        instance.getStringParameter(PayolaStorage.askQueryParameter)
    }

    def executeQuery(instance: PluginInstance, query: String): Graph = {
        usingDefined(instance.getStringParameter(PayolaStorage.groupURIParameter)) { groupURI =>
            if (storageComponent == null) {
                throw new PluginException("The storage component is null. " +
                    "The plugin has to be instantiated with non-null storage component.")
            }

            // Don't allow the users to specify other graph URIs.
            val sparqlQuery = QueryFactory.create(query)
            sparqlQuery.getGraphURIs.clear()

            storageComponent.rdfStorage.executeSPARQLQuery(sparqlQuery.toString, groupURI)
        }
    }

    def askQuery(instance: PluginInstance): Boolean = {
        usingDefined(instance.getStringParameter(PayolaStorage.groupURIParameter), instance.getStringParameter(PayolaStorage.askQueryParameter)) {
            (groupURI, query) =>
            if (storageComponent == null) {
                throw new PluginException("The storage component is null. " +
                    "The plugin has to be instantiated with non-null storage component.")
            }

            // Don't allow the users to specify other graph URIs.
            val sparqlQuery = QueryFactory.create(query)
            sparqlQuery.getGraphURIs.clear()

            storageComponent.rdfStorage.executeSPARQLAskQuery(sparqlQuery.toString, groupURI).toBoolean
        }
    }

    def askQuerySource(instance: PluginInstance, query: String): Boolean = {
        usingDefined(instance.getStringParameter(PayolaStorage.groupURIParameter)) {
            (groupURI) =>
                if (storageComponent == null) {
                    throw new PluginException("The storage component is null. " +
                        "The plugin has to be instantiated with non-null storage component.")
                }

                // Don't allow the users to specify other graph URIs.
                val sparqlQuery = QueryFactory.create(query)
                sparqlQuery.getGraphURIs.clear()

                storageComponent.rdfStorage.executeSPARQLAskQuery(sparqlQuery.toString, groupURI).toBoolean
        }
    }
}

object PayolaStorage
{
    val pluginName = "Payola Private Storage"

    val groupURIParameter = "Group URI"

    val askQueryParameter = "ASK query"
}
