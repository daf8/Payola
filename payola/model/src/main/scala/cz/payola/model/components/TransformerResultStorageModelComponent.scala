package cz.payola.model.components

import cz.payola.domain.RdfStorageComponent
import cz.payola.domain.entities.User
import cz.payola.common.rdf._
import cz.payola.domain.entities.TransformerResult
import cz.payola.data.DataContextComponent
import scala.collection._
import cz.payola.domain.entities.plugins.concrete.data.PayolaStorage
import cz.payola.common.rdf.Graph
import java.io._
import scala.actors.Futures._

import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model._
import org.apache.jena.riot._
import org.apache.jena.riot.lang._
import cz.payola.domain.rdf._

trait TransformerResultStorageModelComponent
{
    self: DataContextComponent with RdfStorageComponent =>

    lazy val transformerResultStorageModel = new
        {
            private def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
                val p = new java.io.PrintWriter(f, "UTF-8")
                try { op(p) } finally { p.close() }
            }

            def queryProperties(evaluationId: String, query: String) : scala.collection.Seq[String] = {

                val graph = rdfStorage.executeSPARQLQuery(query, constructUri(evaluationId))
                graph.edges
                    .filter(_.uri == "http://www.w3.org/2005/sparql-results#value").map(_.destination.toString)
                    .filterNot(_.startsWith("http://schema.org"))
            }

            def saveGraph(graph: Graph, transformerId: String, evaluationId: String) {

                transformerResultRepository.storeResult(new TransformerResult(
                    transformerId, None, evaluationId, graph.vertices.size,
                    new java.sql.Timestamp(System.currentTimeMillis)))

                val uri = constructUri(evaluationId)
                rdfStorage.storeGraphGraphProtocol(uri, graph.asInstanceOf[cz.payola.domain.rdf.Graph])
            }

            /**
             * Checks if the evaluation if stored.
             */
            def exists(evaluationId: String) = transformerResultRepository.exists(evaluationId)

            def transformerId(evaluationId: String) : String = {
                transformerResultRepository.byEvaluationId(evaluationId).map{r => r.transformerId}.getOrElse("")
            }

            /**
             * Get whole graph
             */
            def getGraph(evaluationId: String): Graph = {
                getGraph("CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o.}", evaluationId)
            }

            def getGraph(sparqlQuery: String, evaluationId: String): Graph = {
                val graphSize = rdfStorage.executeSPARQLQuery("SELECT (COUNT(*) as ?graphsize) WHERE {?s ?p ?o.}", constructUri(evaluationId))
                val graphVerticesCount = graphSize.edges.find(_.uri.contains("value")).map(_.destination.toString().toLong)

                val graph = rdfStorage.executeSPARQLQuery(sparqlQuery, constructUri(evaluationId), graphVerticesCount)
                transformerResultRepository.updateTimestamp(evaluationId)
                graph
            }

            def getGraphJena(evaluationId: String, format: String = "RDF/JSON"): String = {
                val dataset = rdfStorage.executeSPARQLQueryJena("CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o.}", constructUri(evaluationId))
                transformerResultRepository.updateTimestamp(evaluationId)

                val outputStream = new java.io.ByteArrayOutputStream()
                if (format.toLowerCase == "json-ld"){
                    com.github.jsonldjava.jena.JenaJSONLD.init()
                    RDFDataMgr.write(outputStream, dataset, com.github.jsonldjava.jena.JenaJSONLD.JSONLD)
                }else{
                    dataset.getDefaultModel().write(outputStream, format)
                }

                new String(outputStream.toByteArray(),"UTF-8")

            }

            def getGraph(sparqlQueryList: List[String], evaluationId: String): Graph = {
                val graphSize = rdfStorage.executeSPARQLQuery("SELECT (COUNT(*) as ?graphsize) WHERE {?s ?p ?o.}", constructUri(evaluationId))
                val graphVerticesCount = graphSize.edges.find(_.uri.contains("value")).map(_.destination.toString().toLong)

                val graph = sparqlQueryList.map{ query =>
                    rdfStorage.executeSPARQLQuery(query, constructUri(evaluationId), graphVerticesCount)
                }.reduce(_ + _)
                transformerResultRepository.updateTimestamp(evaluationId)
                graph
            }

            def removeGraph(evaluationId: String, transformerId: String) {
                rdfStorage.deleteGraph(constructUri(evaluationId))
                transformerResultRepository.deleteResult(evaluationId, transformerId)
            }

            private def constructUri(evaluationId: String): String = {
                "http://"+evaluationId
            }

            def getEmptyGraph(): Graph = {
                JenaGraph.empty
            }
        }

    val maxStoredTransformers: Long

    val maxStoredTransformersPerUser: Long
}