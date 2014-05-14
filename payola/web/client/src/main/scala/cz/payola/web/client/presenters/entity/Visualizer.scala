package cz.payola.web.client.presenters.entity

import s2js.adapters.browser._
import cz.payola.web.client.views.entity.VisualizerView
import cz.payola.web.shared.VisualizerData

import cz.payola.web.client.events.EventArgs
import cz.payola.web.client.Presenter
import cz.payola.web.client.views.bootstrap.modals.AlertModal
import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.graph.PluginView
import cz.payola.web.client.views.graph.table._
import cz.payola.web.client.views.graph.visual.techniques.circle.CircleTechnique
import cz.payola.web.client.views.graph.visual.techniques.tree.TreeTechnique
import cz.payola.web.client.views.graph.visual.techniques.gravity.GravityTechnique
import cz.payola.web.client.views.graph.visual.ColumnChartPluginView
import cz.payola.web.client.views.graph.sigma.GraphSigmaPluginView
import cz.payola.web.client.views.graph.datacube._
import cz.payola.web.client.views.map._
import cz.payola.web.client.views.d3.packLayout._
import cz.payola.web.client.views.datacube.DataCubeVisualizer

/**
 * Presenter responsible for the logic of the Analysis Builder editor.
 * @param parentElementId ID of the DOM element to render views into
 */
class Visualizer(parentElementId: String) extends Presenter
{
    protected val parentElement = document.getElementById(parentElementId)

    protected val prefixPresenter = new PrefixPresenter

    val prefixApplier = prefixPresenter.prefixApplier

    val plugins = List[PluginView[_]](
        new TripleTablePluginView(Some(prefixApplier)),
        new SelectResultPluginView(Some(prefixApplier)),
        new CircleTechnique(Some(prefixApplier)),
        new TreeTechnique(Some(prefixApplier)),
        new GravityTechnique(Some(prefixApplier)),
        new ColumnChartPluginView(Some(prefixApplier)),
        new GraphSigmaPluginView(Some(prefixApplier)),
        new TimeHeatmap(Some(prefixApplier)),
        new Generic(Some(prefixApplier)),
        new GoogleMapView(Some(prefixApplier)),
        new GoogleHeatMapView(Some(prefixApplier)),
        new ArcGisMapView(Some(prefixApplier)),
        new PackLayout(Some(prefixApplier)),
        new Sunburst(Some(prefixApplier)),
        new ZoomableSunburst(Some(prefixApplier)),
        new ZoomableTreemap(Some(prefixApplier)),
        new DataCubeVisualizer(Some(prefixApplier))
    )

    def initialize() {
        blockPage("Loading visualizer data...")
        prefixPresenter.initialize
        val view = new VisualizerView("Visualizer check", prefixPresenter.prefixApplier)
        view.render(parentElement)
        view.initialize(view)
        bindAskButtonClickedEvent(view)
        unblockPage()
    }

    private def bindAskButtonClickedEvent(view: VisualizerView) {
        view.askButtonClicked += onAskClicked(view)
    }

    protected def onAskClicked(view: VisualizerView): (EventArgs[VisualizerView]) => Unit = {
        evt =>
            var i = 0
            blockPage("Checking analyses and transformers")
            plugins.map {
                p =>
                    VisualizerData.checkInput(p.name,p.ask){
                        r =>
                            i=i+1
                            if(i==plugins.length){
                                AlertModal.display("Analyses and transformer check successful",i+" visualizers checked")
                                unblockPage()
                            }
                    }{
                        err =>
                            fatalErrorHandler(err)
                    }
            }
            false
    }
}
