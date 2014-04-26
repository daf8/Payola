package cz.payola.web.client.presenters.components

import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.elements.Div
import cz.payola.web.client.views.elements.Anchor
import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.views.elements.form.fields._
import cz.payola.web.shared.Payola

class TransformerControls() extends ComposedView
{

    val runBtnCaption = new Text("Run Transformer")
    private val runBtnIcon = new Icon(Icon.play, true)
    val runBtn = new Button(runBtnCaption, "btn btn-success", runBtnIcon)

    val checkBtnCaption = new Text("Check data sources")
    private val checkBtnIcon = new Icon(Icon.check, true)
    val checkBtn = new Button(checkBtnCaption, "btn btn-success", checkBtnIcon)

    val progressBar = new ProgressBar()
    val stopButton = new Button(new Text("Stop"), "btn-danger disabled", new Icon(Icon.stop, true))
    stopButton.setIsEnabled(false)

    private val timeoutInfoCaptionPre = new Text("Elapsed [sec.]: ")
    val timeoutInfo = new Text("0")
    val checkInfo = new Text("")
    val checkIconTrue = new Icon(Icon.ok,true)
    val checkIconFalse = new Icon(Icon.remove,true)

    val btnDiv = new Div(List(runBtn, stopButton),"col-lg-3")
    val progressDiv = new Div(List(progressBar), "col-lg-6")
    val timeoutInfoBar = new Span(List(timeoutInfoCaptionPre, timeoutInfo), "none col-lg-3")
    val checkInfoLastCheck = new Div(List(new Text("")),"")
    val checkInfoBar = new Div(List(checkIconTrue,checkIconFalse,checkInfo,checkInfoLastCheck), "col-lg-6")
    val checkDiv = new Div(List(checkBtn),"col-lg-2")
    checkInfoLastCheck.setAttribute("id","checkInfoLastCheck")

    private val wrap = new Div(List(btnDiv, progressDiv, timeoutInfoBar), "transformer-controls panel-body")
    private val wrap2 = new Div(List(checkDiv, checkInfoBar), "transformer-controls panel-body")
    private val panel = new Div(List(wrap,wrap2), "panel panel-default")

    def createSubViews = List(panel)
}
