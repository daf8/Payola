package cz.payola.web.client.views.entity.transformer

import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.views.elements._

/**
  *
  */
class EditorCanvasView extends ComposedView
 {

     val canvas = new Div(List(),"analysis-canvas")

     def createSubViews = List(canvas)
 }
