package cz.payola.model.components

import cz.payola.data._
import cz.payola.domain.entities._
import cz.payola.model._
import scala.Some
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import cz.payola.domain.DomainException
import java.net.ConnectException
import cz.payola.domain.rdf._
import java.io.InputStream
import java.io.ByteArrayInputStream

trait
VisualizerModelComponent extends EntityModelComponent
{
    self: DataContextComponent with PrivilegeModelComponent =>

    lazy val visualizerModel = new ShareableEntityModel(visualizerRepository, classOf[Visualizer])
    {
        def addAnalysisChecking(plugin: String, analysis: Analysis, owner: Option[User]) {
            getByName(plugin).map {
                v =>
                    v.addAnalysisChecking(plugin, analysis)
            }.getOrElse {
                val visualizer = new Visualizer(plugin, owner)
                persist(visualizer)
                visualizer.addAnalysisChecking(plugin, analysis)
            }
        }

        def addTransformerChecking(plugin: String, transformer: Transformer, owner: Option[User]) {
            getByName(plugin).map {
                a =>
                    a.addTransformerChecking(plugin, transformer)
            }.getOrElse {
                val visualizer = new Visualizer(plugin, owner)
                persist(visualizer)
                visualizer.addTransformerChecking(plugin, transformer)
            }
        }

        def removeChecking(plugin: String) {
            getByName(plugin).map {
                a =>
                    a.compatibilityAnalysisChecks.foreach {
                        b =>
                            a.removeAnalysisChecking(b)
                    }
                    a.compatibilityTransformerChecks.foreach {
                        b =>
                            a.removeTransformerChecking(b)
                    }
            }.getOrElse {
                throw new Exception("Unknown transformer.")
            }
        }

        def executeQuery(ttl: String, query: String): Boolean = {
            val model: Model = ModelFactory.createDefaultModel()
            val is: InputStream = new ByteArrayInputStream(ttl.getBytes("UTF-8"))
            model.read(is,null,"TTL")
            val graph = new JenaGraph(model)
            graph.executeSPARQLAskQuery(query)
        }
    }
}