package cz.payola.web.client.views.graph.visual

import animation.Animation
import cz.payola.web.client.views.graph.PluginView
import collection.mutable.ListBuffer
import settings.components.visualsetup.VisualSetup
import cz.payola.web.client.events._
import cz.payola.common.rdf._
import cz.payola.web.client.presenters.components.ZoomControls
import cz.payola.web.client.views.elements._
import cz.payola.web.client._
import cz.payola.web.client.views.algebra._
import s2js.adapters.js.browser.window
import s2js.adapters.js._
import s2js.compiler.javascript
import cz.payola.web.client.views.graph.visual.graph._
import cz.payola.web.client.views._
import s2js.adapters.js.browser.document
import cz.payola.web.client.views.bootstrap.Icon
import s2js.adapters.js.dom.CanvasContext

/**
  * Representation of visual based output drawing plugin
  */
abstract class VisualPluginView(settings: VisualSetup, name: String) extends PluginView(name)
{
    private var hoveringOverVertex: Option[VertexView] = None

    protected var mouseIsPressed = false

    private var mousePressedVertex = false

    private var mouseIsDragging = false

    private var mouseDownPosition = Point2D(0, 0)

    val mouseDragged = new BrowserEvent[Canvas]

    var graphView: Option[views.graph.visual.graph.GraphView] = None

    protected val topLayer = new Canvas()

    private val layerPack = new CanvasPack(new Canvas(), new Canvas(), new Canvas(), new Canvas())

    private var topLayerOffset = Vector2D(0, 0)

    private val layers = List(
        layerPack.edgesDeselected,
        layerPack.edgesSelected,
        layerPack.verticesDeselected,
        layerPack.verticesSelected,
        topLayer
    )

    private val zoomControls = new ZoomControls(100)

    private val pngDownloadButton = new Button(new Text("Download as PNG"), "pull-right",
        new Icon(Icon.download)).setAttribute("style", "margin: 0 5px;")

    window.onresize = { e =>
        fitCanvas()
        redraw()
        true
    }

    topLayer.mousePressed += { e =>
        mouseIsPressed = true
        mouseIsDragging = false
        mouseDownPosition = getPosition(e)
        onMouseDown(e)
        false
    }

    topLayer.mouseReleased += { e =>
        mouseIsPressed = false
        onMouseUp(e)
        false
    }

    topLayer.mouseMoved += { e =>
        if (mouseIsPressed) {
            mouseDragged.trigger(e)
        } else {
            onMouseMove(e)
        }
        true
    }

    mouseDragged += { e =>
        mouseIsDragging = true
        onMouseDrag(e)
        false
    }

    topLayer.mouseDoubleClicked += { event =>
        graphView.foreach { g =>
            val vertex = g.getTouchedVertex(getPosition(event))
            vertex.foreach { v =>
                g.selectVertex(vertex.get)
                vertexBrowsing.trigger(new VertexEventArgs[this.type](this, v.vertexModel))
            }
        }
        false
    }

    topLayer.mouseWheelRotated += { event => //zoom - invoked by mouse
        val mousePosition = getPosition(event)
        val scrolled = event.wheelDelta

        if (scrolled < 0) {
            if (zoomControls.canZoomIn) {
                zoomIn(mousePosition)
                zoomControls.increaseZoomInfo()
            }
        } else {
            if (zoomControls.canZoomOut) {
                zoomOut(mousePosition)
                zoomControls.decreaseZoomInfo()
            }
        }
        false
    }

    zoomControls.zoomDecreased += { e =>
        if (graphView.isDefined && zoomControls.canZoomOut) {
            zoomOut(graphView.get.getGraphCenter) //zooming from the center of the graph
            zoomControls.decreaseZoomInfo()
        }
        false
    }

    zoomControls.zoomIncreased += { event => //zoom - invoked by zoom control button
        if (graphView.isDefined && zoomControls.canZoomIn) {
            zoomIn(graphView.get.getGraphCenter) //zooming to the center of the graph
            zoomControls.increaseZoomInfo()
        }
        false
    }

    pngDownloadButton.mouseClicked += { e =>
        val c = new Canvas()
        c.domElement.width = topLayer.domElement.width
        c.domElement.height = topLayer.domElement.height

        c.domElement.getContext[CanvasContext]("2d").drawImage(layerPack.edgesDeselected.domElement,0,0)
        c.domElement.getContext[CanvasContext]("2d").drawImage(layerPack.edgesSelected.domElement,0,0)
        c.domElement.getContext[CanvasContext]("2d").drawImage(layerPack.verticesDeselected.domElement,0,0)
        c.domElement.getContext[CanvasContext]("2d").drawImage(layerPack.verticesSelected.domElement,0,0)
        c.domElement.getContext[CanvasContext]("2d").drawImage(topLayer.domElement,0,0)

        window.open(c.domElement.toDataURL("image/png"))
        false
    }


    //on mouse wheel event work-around###################################################################################

    @javascript(
        """
           /* DOMMouseScroll is for mozilla. */
           self.topLayer.domElement.addEventListener('DOMMouseScroll', function(event) {
               return self.topLayer.mouseWheelRotated.triggerDirectly(self.topLayer, event);
           });
        """)
    private def setMouseWheelListener() {}

    private var hoverExit = new SimpleUnitEvent[Boolean]

    def createSubViews = layers

    override def render(parent: dom.Element) {
        super.render(parent)

        setMouseWheelListener()
        fitCanvas()
    }

    override def updateGraph(graph: Option[Graph]) {
        // If the graph has changed, update the graph view.
        if (graph != currentGraph) {
            if(graph.isDefined) {
                if (graphView.isDefined) {
                    graphView.get.update(graph.get)
                } else {
                    graphView = Some(new views.graph.visual.graph.GraphView(settings))
                }
            } else {
                if (graphView.isDefined) {
                    layers.foreach(_.clear())
                    graphView = None
                    mouseIsDragging = false
                    mousePressedVertex = false
                    mouseDownPosition = Point2D(0, 0)
                }
            }
        }

        super.updateGraph(graph)
    }

    override def destroy() {
        super.destroy()

        graphView = None
        mouseIsDragging = false
        mousePressedVertex = false
        mouseDownPosition = Point2D(0, 0)
    }

    override def renderControls(toolbar: dom.Element) {
        zoomControls.render(toolbar)
        pngDownloadButton.render(toolbar)
    }

    override def destroyControls() {
        zoomControls.destroy()
        pngDownloadButton.destroy()
    }

    protected def redrawQuick() {
        //TODO rename or move somewhere else
        if (!graphView.isEmpty) {
            graphView.get.redraw(layerPack, RedrawOperation.Animation)
        }
    }

    def redraw() {
        if (!graphView.isEmpty) {
            graphView.get.redrawAll(layerPack)
        }
    }

    def redrawSelection() {
        if (graphView.isDefined) {
            graphView.get.redraw(layerPack, RedrawOperation.Selection)
        }
    }

    def size: Vector2D = topLayer.size

    def size_=(size: Vector2D) {

        layers.foreach(_.size = size)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //mouse event handler routines///////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
      * Description of mouse-button-down event. Is called from the layer (canvas) binded to it in the initialization.
      */
    private def onMouseDown(eventArgs: BrowserEventArgs[Canvas]) {
        val position = getPosition(eventArgs)
        var resultedAnimation: Option[Animation[ListBuffer[InformationView]]] = None
        val vertex = graphView.get.getTouchedVertex(position)

        if (vertex.isDefined) {
            // Mouse down near a vertex.
            if (eventArgs.shiftKey) {
                //change selection of the pressed one
                graphView.get.invertVertexSelection(vertex.get)
                if (vertex.get.selected) {
                    val toAnimate = ListBuffer[InformationView]()
                    if (vertex.get.information.isDefined) {
                        toAnimate += vertex.get.information.get
                    }
                    toAnimate ++= getEdgesInformations(vertex.get)
                    resultedAnimation = Some(new Animation(Animation.showText, toAnimate, None,
                        redrawSelection, redrawSelection, None))
                } else {
                    redrawSelection()
                }
            } else {
                //deselect all and select the pressed one
                if (!vertex.get.selected) {
                    graphView.get.deselectAll()
                }
                if (graphView.get.selectVertex(vertex.get)) {
                    val toAnimate = ListBuffer[InformationView]()
                    if (vertex.get.information.isDefined) {
                        toAnimate += vertex.get.information.get
                    }
                    toAnimate ++= getEdgesInformations(vertex.get)
                    resultedAnimation = Some(new Animation(Animation.showText, toAnimate, None,
                        redrawSelection, redrawSelection, None))
                } else {
                    redrawSelection()
                }
            }
            mousePressedVertex = true
            vertexSelected.trigger(new VertexEventArgs[this.type](this, vertex.get.vertexModel))
        } else {
            mousePressedVertex = false
        }


        if (resultedAnimation.isDefined) {
            resultedAnimation.get.run()
        }
    }

    /**
      * goes through all edges of the vertex and returns informations of those,
      * which have selected both of their vertices
      * @return
      */
    private def getEdgesInformations(vertexView: VertexView): ListBuffer[InformationView] = {
        val result = ListBuffer[InformationView]()
        vertexView.edges.foreach { edgeView =>
            if (edgeView.originView.selected && edgeView.destinationView.selected) {
                result += edgeView.information
            }
        }
        result
    }

    private def onMouseUp(eventArgs: BrowserEventArgs[Canvas]) {
        if (!mouseIsDragging && !mousePressedVertex && !eventArgs.shiftKey) {
            //deselect all

            graphView.get.deselectAll()
            redrawSelection()
        }
    }

    /**
      * Description of mouse-move event. Is called from the layer (canvas) binded to it in the initialization.
      */
    private def onMouseDrag(eventArgs: BrowserEventArgs[Canvas]) {
        val end = getPosition(eventArgs)
        if (mousePressedVertex) {
            Animation.clearCurrentTimeout()
            val difference = end - mouseDownPosition

            graphView.get.moveAllSelectedVertices(difference)

            graphView.get.redraw(layerPack, RedrawOperation.Movement)
        } else {
            Animation.clearCurrentTimeout()
            val difference = end - mouseDownPosition

            graphView.get.moveAllVertices(difference)

            graphView.get.redraw(layerPack, RedrawOperation.All)
        }
        mouseDownPosition = end
    }

    /**
      * Is supposed to be called only when mouse is not being dragged (no mouse button is pressed during mouse movement)
      */
    private def onMouseMove(eventArgs: BrowserEventArgs[Canvas]) {
        val position = getPosition(eventArgs)
        val vertex = graphView.get.getTouchedVertex(position)

        if(vertex.isDefined) {
            if(hoveringOverVertex.isEmpty || (!hoveringOverVertex.get.eq(vertex.get))) {
                hoveringOverVertex = vertex
                val infoTable = new VertexInfoTable(hoveringOverVertex.get.getLiteralVertices)
                infoTable.render(document.body)
                hoverExit = new SimpleUnitEvent[Boolean]
                hoverExit += { event => infoTable.destroy() }
            }
        } else if(hoveringOverVertex.isDefined) {
            hoverExit.triggerDirectly(true)
            hoveringOverVertex = None
        }

    }

    private def zoomIn(mousePosition: Point2D) {
        alterVertexPositions(1 + zoomControls.zoomStep, (- mousePosition.toVector) * zoomControls.zoomStep)
    }

    private def zoomOut(mousePosition: Point2D) {
        alterVertexPositions(1 - zoomControls.zoomStep, (mousePosition.toVector) * zoomControls.zoomStep)
    }

    private def alterVertexPositions(positionMultiplier: Double, positionCorrection: Vector2D) {
        graphView.get.getAllVertices.foreach { vv =>
            vv.position = (vv.position * positionMultiplier) + positionCorrection
        }
        redraw()
    }

    private def getPosition(eventArgs: BrowserEventArgs[Canvas]): Point2D = {
        Point2D(eventArgs.clientX - topLayerOffset.x, eventArgs.clientY - topLayerOffset.y)
    }

    def fitCanvas() {
        topLayerOffset = calculateTopLayerOffset
        val layerSize = Vector2D(window.innerWidth, window.innerHeight) - topLayerOffset
        layers.foreach(_.size = layerSize)
    }

    @javascript("""
        var offsetTop = 0;
        var element = self.topLayer.domElement;
        while (element != null) {
            offsetTop += element.offsetTop;
            element = element.offsetParent;
        }
        return new cz.payola.web.client.views.algebra.Vector2D(0, offsetTop);
    """)
    private def calculateTopLayerOffset: Vector2D = Vector2D(0, 0)
}
