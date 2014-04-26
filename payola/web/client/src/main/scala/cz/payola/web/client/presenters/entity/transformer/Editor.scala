package cz.payola.web.client.presenters.entity.transformer

import s2js.adapters.html
import cz.payola.web.client.Presenter
import cz.payola.common.entities.Transformer
import cz.payola.common.entities.Plugin
import cz.payola.web.client.models.Model
import cz.payola.web.client.views.entity.transformer.EditorView

/**
 *
 */
class Editor(val viewElement: html.Element, transformerId: String) extends Presenter
{
    def initialize() {
        blockPage("Loading transformer data...")
        loadTransformerById(transformerId)(initializeView)
    }

    private def initializeView(transformer: Transformer) {
        loadPlugins({
            plugins =>
                val view = new EditorView(transformer)
                view.render(viewElement)
                bindEvents(view)
                unblockPage()
        })(fatalErrorHandler)
    }

    private def loadTransformerById(transformerId: String)(successCallback: Transformer => Unit) {
        Model.getOwnedTransformerById(transformerId)(successCallback)(fatalErrorHandler)
    }

    private def loadPlugins(successCallback: Seq[Plugin] => Unit)(errorHandler: Throwable => Unit) {
        Model.accessiblePlugins(successCallback)(errorHandler)
    }

    private def bindEvents(view: EditorView) {
        view.toolbarView.addDataSource.mouseClicked
        view.toolbarView.addPlugin.mouseClicked
        view.toolbarView.addFork.mouseClicked
        view.toolbarView.addJoin.mouseClicked

        //bind parameters update
        //bind rename
    }
}
