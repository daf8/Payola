package cz.payola.web.shared

import cz.payola.common.entities._
import s2js.compiler._
import cz.payola.domain.entities.User
import cz.payola.common.entities.plugins._
import java.util.Date

@secured
@remote object VisualizerData
{
    @async def checkInput(plugin: String, query: String, user: User = null)(successCallback: (Int => Unit))
        (failCallback: (Throwable => Unit)) {
        var i: Int = 0
        Payola.model.visualizerModel.removeChecking(plugin)
        val analyses = Payola.model.analysisModel.getAccessibleToUser(Some(user))
        val transformers = Payola.model.transformerModel.getAccessibleToUser(Some(user))
        analyses.map {
            a =>
                if(Payola.model.visualizerModel.executeQuery(a.ttl,query)){
                    Payola.model.visualizerModel.addAnalysisChecking(plugin, a, Some(user))
                    i=i+1
                }
        }
        transformers.map {
            t =>
                if(Payola.model.visualizerModel.executeQuery(t.ttl,query)){
                    Payola.model.visualizerModel.addTransformerChecking(plugin, t, Some(user))
                    i=i+1
                }
        }
        successCallback(i)
    }
}
