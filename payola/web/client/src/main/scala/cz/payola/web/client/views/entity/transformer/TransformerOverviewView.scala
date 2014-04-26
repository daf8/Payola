package cz.payola.web.client.views.entity.transformer

import cz.payola.web.client.presenters.components.TransformerControls
import cz.payola.web.client.views.ComposedView
import cz.payola.common.entities.Transformer
import cz.payola.web.client.models.PrefixApplier

class TransformerOverviewView(transformer: Transformer, prefixApplier: PrefixApplier) extends ComposedView
{
    val controls = new TransformerControls()

    val transformerVisualizer = new ReadOnlyTransformerVisualizer(transformer, prefixApplier)

    def createSubViews = List(controls, transformerVisualizer)
}
