package cz.payola.web.client.presenters.entity

import cz.payola.web.client.Presenter
import s2js.adapters.dom
import s2js.adapters.html

/**
 * Presenter which controls the logic of a bunch of CheckButtons. It creates an instance of CheckButtonPresenter for
 * each of the passed placeHolder passed in the first parameter.
 * @param placeHolders List of HTML elements which should contain a check button.
 */
class CheckPresenter(placeHolders: dom.NodeList[html.Element], entityType: String) extends Presenter
{
    def initialize() {
        var i = 0
        while (i < placeHolders.length) {
            val placeHolder = placeHolders.item(i)
            new CheckButtonPresenter(
                placeHolder,
                placeHolder.getAttribute("data-id")
            ).initialize()
            i += 1
        }
    }
}
