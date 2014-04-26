package cz.payola.web.client.views.entity.transformer

import cz.payola.common.entities.Transformer
import cz.payola.common.entities
import cz.payola.web.client.events.SimpleUnitEvent
import cz.payola.web.client.presenters.models.ParameterValue
import cz.payola.common.entities.plugins.TransformerPluginInstance
import cz.payola.web.client.views.entity.plugins._
import custom.DataCubeEditablePluginInstanceView
import cz.payola.web.client.models.PrefixApplier

/**
 *
 * @param transformer
 * @param prefixApplier
 * @author Jiri Helmich
 */
// Updated by Jiri Helmich to enable dynamic loading of PluginInstanceView
class EditableTransformerVisualizer(transformer: Transformer, prefixApplier: PrefixApplier)
    extends TransformerVisualizer(transformer)
{
    val parameterValueChanged = new SimpleUnitEvent[ParameterValue]

    val connectButtonClicked = new SimpleUnitEvent[EditableTransformerPluginInstanceView]

    val deleteButtonClicked = new SimpleUnitEvent[EditableTransformerPluginInstanceView]

    val askButtonClicked = new SimpleUnitEvent[EditableTransformerPluginInstanceView]

    val instanceFactory = new PluginInstanceViewFactory(prefixApplier)

    def createPluginInstanceView(instance: TransformerPluginInstance): TransformerPluginInstanceView = {
        val view = instanceFactory.transformerCreateEditable(transformer, instance, List())

        initializeEditableInstance(view, instance, transformer)
        view
    }

    private def initializeEditableInstance(instanceView: EditableTransformerPluginInstanceView,
        instance: entities.plugins.TransformerPluginInstance,
        transformer: Transformer) {
        instanceView.hideControls()

        if (instanceHasNoFollowers(transformer, instance)) {
            instanceView.showControls()
        }

        instanceView.parameterValueChanged += {
            e => parameterValueChanged.triggerDirectly(e.target)
        }
        instanceView.connectButtonClicked += {
            e => connectButtonClicked.triggerDirectly(e.target)
        }
        instanceView.deleteButtonClicked += {
            e => deleteButtonClicked.triggerDirectly(e.target)
        }
        instanceView.askButtonClicked += {
            e => askButtonClicked.triggerDirectly(e.target)
        }
    }

    private def instanceHasNoFollowers(transformer: Transformer, instance: entities.plugins.TransformerPluginInstance): Boolean = {
        !transformer.pluginInstanceBindings.find(_.sourcePluginInstance == instance).isDefined
    }
}
