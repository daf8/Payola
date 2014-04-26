package cz.payola.web.client.views.entity.transformer

import cz.payola.common.entities.Transformer
import cz.payola.common.entities.plugins.TransformerPluginInstance
import cz.payola.web.client.views.entity.plugins._
import cz.payola.web.client.models.PrefixApplier

class ReadOnlyTransformerVisualizer(transformer: Transformer, prefixApplier: PrefixApplier) extends TransformerVisualizer(transformer)
{
    val instanceFactory = new PluginInstanceViewFactory(prefixApplier)

    def createPluginInstanceView(instance: TransformerPluginInstance): TransformerPluginInstanceView = {
        val result = instanceFactory.transformerCreate(instance, List())

        instancesMap.put(instance.id, result)
        result
    }

    def setInstanceError(instanceId: String, message: String) {
        instancesMap.get(instanceId).map(_.setError(message))
    }

    def setInstanceEvaluated(instanceId: String) {
        instancesMap.get(instanceId).map(_.setEvaluated())
    }

    def setInstanceRunning(instanceId: String) {
        instancesMap.get(instanceId).map(_.setRunning())
    }

    def setAllDone() {
        instancesMap.foreach(_._2.setEvaluated())
    }

    def clearAllAttributes() {
        instancesMap foreach {
            case (key, view) =>
                view.clearStyle()
        }
    }
}
