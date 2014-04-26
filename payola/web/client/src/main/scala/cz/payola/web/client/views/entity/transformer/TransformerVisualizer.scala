package cz.payola.web.client.views.entity.transformer

import cz.payola.web.client.views.elements._
import cz.payola.common.entities.Transformer
import cz.payola.web.client.View
import s2js.adapters.html
import scala.collection.mutable.ArrayBuffer
import cz.payola.common.entities
import scala.collection.mutable.HashMap
import cz.payola.web.client.events.SimpleUnitEvent
import entities.plugins._
import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.views.entity.plugins.TransformerPluginInstanceView

abstract class TransformerVisualizer(transformer: Transformer) extends ComposedView
{
    val pluginInstanceRendered = new SimpleUnitEvent[TransformerPluginInstanceView]
    val paramNameClicked = new SimpleUnitEvent[ParameterValue[_]]

    private val pluginCanvas = new Div(Nil, "plugin-canvas")

    protected val instancesMap: HashMap[String, TransformerPluginInstanceView] = new HashMap[String, TransformerPluginInstanceView]

    def createSubViews: Seq[View] = {
        List(pluginCanvas)
    }

    override def render(parent: html.Element) {
        super.render(parent)
        renderTransformer()
    }

    private def renderTransformer() {
        val sources = new ArrayBuffer[TransformerPluginInstanceView]
        val renderBuffer = new ArrayBuffer[TransformerPluginInstanceView]

        fillRenderBuffers(transformer, sources, renderBuffer)
        setPredecessorsFromBindings(transformer)
        renderSources(sources, renderBuffer)
        renderBufferTopologically(renderBuffer)
    }

    private def renderBufferTopologically(renderBuffer: ArrayBuffer[TransformerPluginInstanceView]) {
        while (!renderBuffer.isEmpty) {
            renderBuffer.map {
                s =>
                    val canRender: Boolean = predecessorsRendered(s, renderBuffer)
                    if (canRender) {
                        renderPluginInstanceView(s)
                        renderBuffer -= s
                    }
            }
        }
    }

    private def predecessorsRendered(s: TransformerPluginInstanceView, renderBuffer: ArrayBuffer[TransformerPluginInstanceView]): Boolean = {
        var canRender = true
        s.predecessors.map {
            predecessor => canRender = (canRender && !renderBuffer.contains(predecessor))
        }
        canRender
    }

    def renderSources(sources: ArrayBuffer[TransformerPluginInstanceView], renderBuffer: ArrayBuffer[TransformerPluginInstanceView]) {
        sources.map { s =>
            renderPluginInstanceView(s)
            renderBuffer -= s
        }
    }

    private def setPredecessorsFromBindings(transformer: entities.Transformer) {
        transformer.pluginInstanceBindings.sortWith((a,b) => (a.targetInputIndex < b.targetInputIndex)).map { b =>
            val buff = new ArrayBuffer[TransformerPluginInstanceView]()
            instancesMap(b.targetPluginInstance.id).predecessors.map(buff.append(_))
            buff.append(instancesMap(b.sourcePluginInstance.id))
            instancesMap(b.targetPluginInstance.id).predecessors = buff
        }
    }

    protected def fillRenderBuffers(transformer: entities.Transformer, sources: ArrayBuffer[TransformerPluginInstanceView],
        renderBuffer: ArrayBuffer[TransformerPluginInstanceView]) {
        transformer.pluginInstances.map { instance =>
            val clientInstance = createPluginInstanceView(instance)
            instancesMap.put(instance.id, clientInstance)

            if (isSource(instance, transformer)) {
                sources += clientInstance
            }

            renderBuffer += clientInstance
        }
    }

    def createPluginInstanceView(instance: TransformerPluginInstance): TransformerPluginInstanceView

    private def isSource(instance: entities.plugins.TransformerPluginInstance, transformer: Transformer): Boolean = {
        !transformer.pluginInstanceBindings.find(_.targetPluginInstance == instance).isDefined
    }

    def renderPluginInstanceView(v: TransformerPluginInstanceView) {
        v.render(pluginCanvas.htmlElement)
        v.parameterNameClicked += { e => paramNameClicked.triggerDirectly(e.target) }
        pluginInstanceRendered.triggerDirectly(v)
    }
}
