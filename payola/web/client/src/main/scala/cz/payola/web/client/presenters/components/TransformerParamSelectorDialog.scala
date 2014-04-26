package cz.payola.web.client.presenters.components

import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.views.entity.transformer.ReadOnlyTransformerVisualizer
import cz.payola.web.client.views.elements.Div
import cz.payola.common.entities.Transformer
import collection.mutable.ArrayBuffer
import s2js.adapters.html
import s2js.adapters.browser._
import scala.Some
import cz.payola.web.client.models.PrefixApplier

/**
 * A plugin used to select parameters of the newly created transformer plugin.
 * @author Jiri Helmich
 */
class TransformerParamSelectorDialog(transformer: Transformer)
    extends Modal("Choose transformer params to be dynamic", Nil, Some("OK"), None, false, "preview-dialog")
{
    val paramIds = new ArrayBuffer[(String, String)]

    val visualizer = new ReadOnlyTransformerVisualizer(transformer, new PrefixApplier())
    visualizer.paramNameClicked += { eventArg =>

        val dialog = new PromptDialog("Enter a new name of the parameter")
        dialog.render()

        dialog.confirming += { x =>
            paramIds += ((eventArg.target.id, dialog.prompt.value))
            dialog.destroy()
            true
        }
    }

    val placeholder = new Div(List())

    override val body = List(placeholder)

    override def render(parent: html.Element = document.body) {
        super.render(parent)
        visualizer.render(placeholder.blockHtmlElement)
    }
}
