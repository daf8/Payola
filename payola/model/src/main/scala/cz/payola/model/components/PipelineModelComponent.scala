package cz.payola.model.components

import cz.payola.model._
import cz.payola.data._
import cz.payola.domain.entities._
import cz.payola.domain.rdf.JenaGraph
import cz.payola.domain.entities.plugins._

trait PipelineModelComponent extends EntityModelComponent
{
    self: DataContextComponent with PrivilegeModelComponent =>
    lazy val pipelineModel = new ShareableEntityModel(pipelineRepository, classOf[Pipeline])
    {
        def x(owner: Option[User], ds: DataSource, a: Analysis){
            val p = new Pipeline("Pipeline",owner,a)

            persist(p)
            p.addDataSource(ds)

            println(p.dataSources)
        }

        /*def addAnalysisChecking(plugin: String, analysis: Analysis, owner: Option[User]) {
            getByName(plugin).map {
                v =>
                    v.addAnalysisChecking(plugin, analysis)
            }.getOrElse {
                val visualizer = new Visualizer(plugin, owner)
                visualizer.isPublic = true
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
                visualizer.isPublic = true
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
            }
        }*/
    }
}
