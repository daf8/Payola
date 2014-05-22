package cz.payola.web.shared

import cz.payola.common.entities._
import s2js.compiler._
import cz.payola.domain.entities.User
import cz.payola.common.entities.plugins._

@secured
@remote object PipelineData
{
    @async def getCompatibleAnalysesWithDataSource(dsID: String, user: User = null)(successCallback: (Seq[Analysis] => Unit))(failCallback: (Throwable => Unit)) {
        val analysis = Payola.model.analysisModel.getAccessibleToUser(Some(user)).filter(_.compatibilityChecks.length>0)
        successCallback(
            analysis.filter{ a =>
                a.compatibilityChecks.map { b =>
                    b.compatibleDataSource.id == dsID
                }.reduceLeft((a,b) => a || b)
            }
        )
    }

    @async def getCompatibleTransformersWithAnalysis(aID: String, user: User = null)(successCallback: (Seq[Transformer] => Unit))(failCallback: (Throwable => Unit)) {
        val transformer = Payola.model.transformerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityChecks.length>0)
        successCallback(
            transformer.filter{ a =>
                a.compatibilityChecks.map { b =>
                    b.compatibleAnalysis.id == aID
                }.reduceLeft((a,b) => a || b)
            }
        )
    }

    @async def getCompatibleTransformersWithTransformer(tID: String, user: User = null)(successCallback: (Seq[Transformer] => Unit))(failCallback: (Throwable => Unit)) {
        val transformer = Payola.model.transformerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityTransformerChecks.length>0)
        successCallback(
            transformer.filter{ a =>
                a.compatibilityTransformerChecks.map { b =>
                    b.compatibleTransformer.id == tID
                }.reduceLeft((a,b) => a || b)
            }
        )
    }

    @async def getCompatibleVisualizerWithTransformer(tID: String, user: User = null)(successCallback: (Seq[Visualizer] => Unit))(failCallback: (Throwable => Unit)) {
        val visualizer = Payola.model.visualizerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityTransformerChecks.length>0)
        successCallback(
            visualizer.filter{ a =>
                a.compatibilityTransformerChecks.map { b =>
                    b.compatibleTransformer.id == tID
                }.reduceLeft((a,b) => a || b)
            }
        )
    }

    @async def getCompatibleVisualizerWithAnalysis(aID: String, user: User = null)(successCallback: (Seq[Visualizer] => Unit))(failCallback: (Throwable => Unit)) {
        val visualizer = Payola.model.visualizerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityAnalysisChecks.length>0)
        successCallback(
            visualizer.filter{ a =>
                a.compatibilityAnalysisChecks.map { b =>
                    b.compatibleAnalysis.id == aID
                }.reduceLeft((a,b) => a || b)
            }
        )
    }

    def transformerRecurse(tID: String, analysis: String, prev: String, user: User): List[List[String]] = {
        var rows: List[List[String]] = List()
        val transformers = Payola.model.transformerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityTransformerChecks.length>0).filter{ a =>
            a.compatibilityTransformerChecks.map { b =>
                b.compatibleTransformer.id == tID
            }.reduceLeft((a,b) => a || b)
        }
        transformers.map { t =>
            rows ++= transformerRecurse(t.id, analysis, prev + ", " + t.name, user)
            val visualizers = Payola.model.visualizerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityTransformerChecks.length>0).filter{ a =>
                a.compatibilityTransformerChecks.map { b =>
                    b.compatibleTransformer.id == t.id
                }.reduceLeft((a,b) => a || b)
            }
            visualizers.map { v =>
                rows ++= List(List(analysis,prev + ", " + t.name,v.name))
            }
        }
        rows
    }

    @async def getCompatibleTableWithDataSource(dsID: String, user: User = null)(successCallback: (List[List[String]] => Unit))(failCallback: (Throwable => Unit)) {
        var rows: List[List[String]] = List()
        val analyses = Payola.model.analysisModel.getAccessibleToUser(Some(user)).filter(_.compatibilityChecks.length>0).filter{ a =>
            a.compatibilityChecks.map { b =>
                b.compatibleDataSource.id == dsID
            }.reduceLeft((a,b) => a || b)
        }
        analyses.map { an =>
            val visualizers = Payola.model.visualizerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityAnalysisChecks.length>0).filter{ a =>
                a.compatibilityAnalysisChecks.map { b =>
                    b.compatibleAnalysis.id == an.id
                }.reduceLeft((a,b) => a || b)
            }
            visualizers.map { v =>
                rows ++= List(List(an.name,"",v.name))
            }
            val transformers = Payola.model.transformerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityChecks.length>0).filter{ a =>
                a.compatibilityChecks.map { b =>
                    b.compatibleAnalysis.id == an.id
                }.reduceLeft((a,b) => a || b)
            }
            transformers.map { t =>
                val visualizers = Payola.model.visualizerModel.getAccessibleToUser(Some(user)).filter(_.compatibilityTransformerChecks.length>0).filter{ a =>
                    a.compatibilityTransformerChecks.map { b =>
                        b.compatibleTransformer.id == t.id
                    }.reduceLeft((a,b) => a || b)
                }
                visualizers.map { v =>
                    rows ++= List(List(an.name,t.name,v.name))
                }
                rows ++= transformerRecurse(t.id,an.name,t.name,user)
            }
        }
        successCallback(rows)
    }
}