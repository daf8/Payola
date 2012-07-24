package cz.payola.web.client.views

import scala.collection._
import s2js.adapters.js.dom
import s2js.adapters.js.browser.document
import cz.payola.web.client.events.BrowserEvent
import cz.payola.web.client.View
import s2js.compiler.javascript
import s2js.adapters.js.browser.window
import cz.payola.web.client.views.elements.Text

abstract class ElementView[A <: dom.Element](domElementName: String, val subViews: Seq[View], cssClass: String)
    extends View
{
    val domElement = document.createElement[A](domElementName)

    val keyPressed = new BrowserEvent[this.type]

    val keyReleased = new BrowserEvent[this.type]

    val mouseClicked = new BrowserEvent[this.type]

    val mouseDoubleClicked = new BrowserEvent[this.type]

    val mousePressed = new BrowserEvent[this.type]

    val mouseReleased = new BrowserEvent[this.type]

    val mouseMoved = new BrowserEvent[this.type]

    val mouseWheelRotated = new BrowserEvent[this.type]

    protected var parentElement: Option[dom.Element] = None

    domElement.onkeydown = { e => keyPressed.triggerDirectly(this, e) }
    domElement.onkeyup = { e => keyReleased.triggerDirectly(this, e) }
    domElement.onclick = { e => mouseClicked.triggerDirectly(this, e) }
    domElement.ondblclick = { e => mouseDoubleClicked.triggerDirectly(this, e) }
    domElement.onmousedown = { e => mousePressed.triggerDirectly(this, e) }
    domElement.onmouseup = { e => mouseReleased.triggerDirectly(this, e) }
    domElement.onmousemove = { e => mouseMoved.triggerDirectly(this, e) }
    domElement.onmousewheel = { e => mouseWheelRotated.triggerDirectly(this, e) }
    addCssClass(cssClass)

    def blockDomElement = domElement

    def render(parent: dom.Element) {
        parentElement = Some(parent)
        parent.appendChild(domElement)
        subViews.foreach { v =>
            new Text(" ").render(domElement)
            v.render(domElement)
        }
    }

    def destroy() {
        parentElement.foreach(_.removeChild(domElement))
    }

    def getAttribute(name: String): String = {
        domElement.getAttribute(name)
    }

    def setAttribute(name: String, value: String): this.type = {
        domElement.setAttribute(name, value)
        this
    }

    def addCssClass(cssClass: String): this.type =  {
        removeCssClass(cssClass)
        setAttribute("class", getAttribute("class") + " " + cssClass)
        this
    }

    def removeCssClass(cssClass: String): this.type =  {
        setAttribute("class", getAttribute("class").replaceAllLiterally(cssClass, ""))
        this
    }

    def id: String = {
        getAttribute("id")
    }

    def id_=(value: String) {
        setAttribute("id", value)
    }

    def removeAllChildNodes() {
        while (domElement.hasChildNodes) {
            domElement.removeChild(domElement.firstChild)
        }
    }
}
