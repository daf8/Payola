package cz.payola.web.client.presenters.entity.transformer

import cz.payola.web.shared.TransformerBuilderData
import cz.payola.web.client.views.entity.transformer._
import cz.payola.web.client.presenters.models.ParameterValue
import cz.payola.common.entities.Transformer
import cz.payola.common.ValidationException
import s2js.adapters.browser.`package`._
import cz.payola.web.client.views.entity.plugins.PluginInstanceViewFactory
import cz.payola.web.client.models.PrefixApplier

/**
 * A variant of TransformerBuilder for editing an existing transformer. Overrides initialize method.
 * @param parentElementId ID of the DOM element to render views into
 * @param transformerIdParam ID of the edited transformer
 */
class TransformerEditor(parentElementId: String, transformerIdParam: String)
    extends TransformerBuilder(parentElementId)
{
    transformerId = transformerIdParam

    override def initialize() {
        blockPage("Loading transformer data...")
        prefixPresenter.initialize
        TransformerBuilderData.getTransformer(transformerId) { transformer =>

            instanceViewFactory = new PluginInstanceViewFactory(prefixPresenter.prefixApplier)

            lockTransformerAndLoadPlugins({ () =>
                val view = new TransformerEditorView(transformer, None, None, None, "Edit transformer", prefixPresenter.prefixApplier)
                view.visualizer.pluginInstanceRendered += { e => instancesMap.put(e.target.pluginInstance.id, e.target)}
                view.render(parentElement)
                bindParameterChangedEvent(view.visualizer)
                bindConnectButtonClickedEvent(view, transformer)
                bindAskButtonClickedEvent(view, transformer)
                bindDeleteButtonClickedEvent(view.visualizer)
                constructBranches(transformer)
                bindMenuEvents(view, transformer)

                view.runButton.mouseClicked += { args =>
                    window.location.href = "/transformer/" + transformerId
                    true
                }

                unblockPage()
            })
            true
        } { error => fatalErrorHandler(error)}
    }

    private def constructBranches(transformer: Transformer) {
        val targets = transformer.pluginInstances.filterNot { pi =>
            transformer.pluginInstanceBindings.find(_.sourcePluginInstance.id == pi.id).isDefined
        }.map { pi => instancesMap.get(pi.id).get}

        targets.foreach(branches.append(_))
    }

    private def bindParameterChangedEvent(visualizer: EditableTransformerVisualizer) {
        visualizer.parameterValueChanged += { e =>
            val pv = e.target
            pv.control.isActive = true
            storeParameterValueToServer(pv)
        }
    }

    private def bindConnectButtonClickedEvent(view: TransformerEditorView, transformer: Transformer) {
        view.visualizer.connectButtonClicked += onConnectClicked(view, transformer)
    }

    private def bindAskButtonClickedEvent(view: TransformerEditorView, transformer: Transformer) {
        view.visualizer.askButtonClicked += onAskClicked(view, transformer)
    }

    private def bindDeleteButtonClickedEvent(visualizer: EditableTransformerVisualizer) {
        visualizer.deleteButtonClicked += onDeleteClick
    }

    private def storeParameterValueToServer(pv: ParameterValue) {
        parameterChangedServerCall(pv)
    }

    private def parameterChangedServerCall(pv: ParameterValue) {
        TransformerBuilderData.setParameterValue(transformerId, pv.pluginInstanceId, pv.name, pv.value) { () =>
            pv.control.isActive = false
            pv.control.setOk()
        } { error =>
            error match {
                case e: ValidationException => {
                    pv.control.isActive = false
                    pv.control.setError("Invalid value")
                }
                case _ => fatalErrorHandler(error)
            }
        }
    }
}
