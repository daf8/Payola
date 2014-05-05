package cz.payola.web.shared

import cz.payola.common.entities._
import cz.payola.common.entities.plugins._
import cz.payola.domain.entities.User
import cz.payola.domain.entities.plugins.concrete.DataFetcher
import s2js.compiler._

@secured
@remote object TransformerBuilderData
{
    @async def createEmptyTransformer(name: String, user: User = null)(successCallback: (Transformer => Unit))
        (failCallback: (Throwable => Unit)) {
        successCallback(Payola.model.transformerModel.create(user, name))
    }

    @async def getPlugins(user: User = null)(successCallback: (Seq[Plugin] => Unit))(failCallback: (Throwable => Unit)) {
        successCallback(Payola.model.pluginModel.getAccessibleToUser(Some(user)))
    }

    @async def getAnalyses(user: User = null)(successCallback: (Seq[Analysis] => Unit))(failCallback: (Throwable => Unit)) {
        successCallback(Payola.model.analysisModel.getAccessibleToUser(Some(user)))
    }

    @async def lockTransformer(id: String, user: User = null)(successCallback: (() => Unit))(failCallback: (Throwable => Unit)) {
        successCallback()
    }

    @async def unlockTransformer(id: String, user: User = null)(successCallback: (() => Unit))(failCallback: (Throwable => Unit)) {
        successCallback()
    }

    @async def setTransformerName(id: String, name: String, user: User = null)(successCallback: (Boolean => Unit))
        (failCallback: (Throwable => Unit)) {
        val transformer = Payola.model.transformerModel.getById(id)
        transformer.map {
            a =>
                a.name = name
                Payola.model.transformerModel.persist(a)
        }
        successCallback(true)
    }

    @async def setTransformerDescription(id: String, description: String, user: User = null)
        (successCallback: (Boolean => Unit))
        (failCallback: (Throwable => Unit)) {
        val transformer = Payola.model.transformerModel.getById(id)
        transformer.map {
            a =>
                a.description = description
                Payola.model.transformerModel.persist(a)
        }
        successCallback(true)
    }

    @async def setTransformerTtl(id: String, ttl: String, user: User = null)
        (successCallback: (Boolean => Unit))
        (failCallback: (Throwable => Unit)) {
        val transformer = Payola.model.transformerModel.getById(id)
        transformer.map {
            a =>
                a.ttl = ttl
                Payola.model.transformerModel.persist(a)
        }
        successCallback(true)
    }

    @async def setTransformerChecked(id: String, checked: Boolean, user: User = null)
        (successCallback: (Boolean => Unit))
        (failCallback: (Throwable => Unit)) {
        val transformer = Payola.model.transformerModel.getById(id)
        transformer.map {
            a =>
                a.checked = checked
                a.lastCheck = System.currentTimeMillis()
                Payola.model.transformerModel.persist(a)
        }
        successCallback(true)
    }

    @async def createPluginInstance(pluginId: String, transformerId: String, user: User = null)
        (successCallback: (TransformerPluginInstance => Unit))
        (failCallback: (Throwable => Unit)) {
        successCallback(Payola.model.transformerModel.createPluginInstance(pluginId, transformerId))
    }

    @async def setParameterValue(transformerId: String, pluginInstanceId: String, parameterName: String, value: String,
        user: User = null)
        (successCallback: (() => Unit))(failCallback: (Throwable => Unit)) {
        Payola.model.transformerModel.setParameterValue(user, transformerId, pluginInstanceId, parameterName, value)
        successCallback()
    }

    @async def saveBinding(transformerId: String, sourceId: String, targetId: String, inputIndex: Int, user: User = null)
        (successCallback: (Boolean => Unit))
        (failCallback: (Throwable => Unit)) {
        Payola.model.transformerModel.addBinding(transformerId, sourceId, targetId, inputIndex)
        successCallback(true)
    }

    @async def deletePluginInstance(transformerId: String, pluginInstanceId: String, user: User = null)
        (successCallback: (Boolean => Unit))
        (failCallback: (Throwable => Unit)) {
        Payola.model.transformerModel.removePluginInstanceById(transformerId, pluginInstanceId)
        successCallback(true)
    }

    @async def getTransformer(transformerId: String, user: User = null)(successCallback: (Transformer => Unit))
        (failCallback: (Throwable => Unit)) {
        val transformer = Payola.model.transformerModel.getById(transformerId)
        successCallback(transformer.get)
    }

    @async def getDataSources(user: User = null)(successCallback: (Seq[DataSource] => Unit))
        (failCallback: (Throwable => Unit)) {
        val sources = Payola.model.dataSourceModel.getAccessibleToUser(Some(user))
        successCallback(sources)
    }

    @async def checkInput(transformerId: String, pluginInstanceId: String, user: User = null)(successCallback: (Int => Unit))
        (failCallback: (Throwable => Unit)) {
        var i: Int = 0
        Payola.model.transformerModel.removeChecking(transformerId, pluginInstanceId)
        val analyses = Payola.model.analysisModel.getAccessibleToUser(Some(user))
        val transformers = Payola.model.transformerModel.getAccessibleToUser(Some(user))
        val query = Payola.model.transformerModel.getAccessibleToUserById(Some(user),transformerId).get.pluginInstances.filter(_.id==pluginInstanceId).head.getStringParameter("ASK query").get
        analyses.map {
            a =>
                if(Payola.model.transformerModel.executeQuery(a.ttl,query)){
                    Payola.model.transformerModel.addChecking(transformerId, pluginInstanceId, a)
                    i=i+1
                }
        }
        transformers.map {
            t =>
                if(Payola.model.transformerModel.executeQuery(t.ttl,query) && t.id != transformerId){
                    Payola.model.transformerModel.addTransformerChecking(transformerId, pluginInstanceId, t)
                    i=i+1
                }
        }
        successCallback(i)
    }
}
