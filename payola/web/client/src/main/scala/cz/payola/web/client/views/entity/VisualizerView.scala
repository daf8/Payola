package cz.payola.web.client.views.entity

import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.views.elements.lists._
import cz.payola.web.client.views.elements.form.fields._
import s2js.adapters.browser.`package`._
import scala.Some
import cz.payola.web.client.models.PrefixApplier
import cz.payola.web.client.events.SimpleUnitEvent

class VisualizerView(pageTitle: String, prefixApplier: PrefixApplier) extends ComposedView
{
    val askButtonClicked = new SimpleUnitEvent[VisualizerView]

    val askButton = new Button(new Text("Execute ASK query on datasources"))

    protected val menu = new UnorderedList(List(askButton),"list-group")

    protected val panelBody = new Div(List(menu), "panel-body")
    protected val leftColContent = new Div(List(panelBody), "panel panel-default")

    protected val leftCol = new Div(List(leftColContent), "col-lg-3")

    val h1 = new Heading(List(new Text(pageTitle)),1,"col-lg-10")

    val mainHeader = new Div(List(h1),"main-header row")
    val row = new Div(List(leftCol))
    protected val container = new Div(List(mainHeader, row))

    def createSubViews = List(container)

    def initialize(instanceView: VisualizerView) {
        askButton.mouseClicked += { e =>
            askButtonClicked.triggerDirectly(this)
            false
        }
    }
}
