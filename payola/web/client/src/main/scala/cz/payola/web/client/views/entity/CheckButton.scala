package cz.payola.web.client.views.entity

import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.views.elements.lists.ListItem

class CheckButton(private var _checked: Boolean) extends ComposedView
{
    private val checkButtonText = new Text("")

    private val checkButtonIcon = new Icon(Icon.check)

    val checkButton = new Button(checkButtonText, "col-lg-1", checkButtonIcon)

    checked = _checked

    def createSubViews = List(checkButton)

    def checked: Boolean = _checked

    def checked_=(value: Boolean) {
        _checked = value
        if (_checked) {
            checkButtonText.text_=(" OK")
            checkButton.removeCssClass("btn-warning")
            checkButton.addCssClass("btn-success")
            //checkButton.setAttribute(Icon,new Icon(Icon.ok))
            //checkButtonIcon.
            //checkButtonText.
            //checkButtonIcon.render(checkButton)
        } else {
            checkButtonText.text_=(" KO")
            checkButton.addCssClass("btn-warning")
            checkButton.removeCssClass("btn-success")
            //checkButton.setAttribute(Icon,new Icon(Icon.remove))
            //checkButtonIcon = new Icon(Icon.remove)
            //checkButtonIcon.render(checkButton)
        }
    }

}
