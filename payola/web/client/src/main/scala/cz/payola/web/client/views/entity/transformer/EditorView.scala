package cz.payola.web.client.views.entity.transformer

import cz.payola.common.entities.Transformer
import cz.payola.web.client.views.ComposedView

/**
 *
 */
class EditorView(transformer: Transformer) extends ComposedView
{
    val toolbarView = new EditorToolbarView
    val canvasView = new EditorCanvasView

    def createSubViews = List(toolbarView, canvasView)
}
