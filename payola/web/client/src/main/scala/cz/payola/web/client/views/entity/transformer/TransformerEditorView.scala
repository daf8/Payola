package cz.payola.web.client.views.entity.transformer

import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.views.ComposedView
import cz.payola.common.entities.Transformer
import cz.payola.web.client.views.elements.lists._
import cz.payola.web.client.views.elements.form.fields._
import s2js.adapters.browser.`package`._
import scala.Some
import cz.payola.web.client.models.PrefixApplier

class TransformerEditorView(transformer: Transformer, newName: Option[String], newDesc: Option[String], newTtl: Option[String], pageTitle: String, prefixApplier: PrefixApplier) extends ComposedView
{
    val name = new InputControl("Transformer name:", new TextInput("name", if(newName.isDefined){newName.get}else{transformer.name}, "Transformer name"), Some("nofloat"), None)

    val description = new InputControl("Description:", new TextArea("description",  if(newDesc.isDefined){newDesc.get}else{transformer.description}, "Anaylsis description"), Some("nofloat"), None)

    val ttl = new InputControl("RDF data sample in turtle:", new TextArea("ttl",  if(newTtl.isDefined){newTtl.get}else{transformer.ttl}, "RDF data sample in turtle"), Some("nofloat"), None)

    val ttlFileInput = new InputControl("Load turtle from file: ", new FileInput("ttlFile", "", "span10"), Some("nofloat"), None)

    protected val properties = new Div(List(name, description, ttl, ttlFileInput))

    val visualizer = new EditableTransformerVisualizer(transformer, prefixApplier)

    protected val panelBody = new Div(List(properties), "panel-body")
    protected val leftColContent = new Div(List(panelBody), "panel panel-default")

    val transformerCanvas = new Div(List(visualizer), "plugin-space")

    protected val leftCol = new Div(List(leftColContent), "col-lg-3")

    protected val rightCol = new Div(List(transformerCanvas), "col-lg-9 relative")

    val h1 = new Heading(List(new Text(pageTitle)),1,"col-lg-10")
    val runButton = new Button(new Text("Run"), "col-lg-1", new Icon(Icon.play))

    val mainHeader = new Div(List(h1, runButton),"main-header row")
    val row = new Div(List(leftCol, rightCol))
    protected val container = new Div(List(mainHeader, row))

    name.field.addCssClass("col-lg-12")
    description.field.addCssClass("col-lg-12")
    ttl.field.addCssClass("col-lg-12")

    def setName(newValue: String) {
        name.field.value = newValue
    }

    def createSubViews = List(container)
}
