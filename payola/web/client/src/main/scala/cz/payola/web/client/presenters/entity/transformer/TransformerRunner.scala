package cz.payola.web.client.presenters.entity.transformer

import s2js.adapters.browser._
import cz.payola.web.client._
import cz.payola.web.client.views.entity.transformer.TransformerRunnerView
import cz.payola.web.shared._
import cz.payola.web.client.presenters.components.TransformerEvaluationSuccessEventArgs
import cz.payola.web.client.events._
import cz.payola.common.entities.Transformer
import cz.payola.web.client.presenters.graph.GraphPresenter
import cz.payola.web.client.views.graph.DownloadButtonView
import cz.payola.web.client.views.bootstrap.modals.AlertModal
import cz.payola.common._
import scala.Some
import cz.payola.common.EvaluationInProgress
import cz.payola.common.EvaluationError
import cz.payola.common.EvaluationSuccess
import cz.payola.web.client.views.VertexEventArgs
import cz.payola.web.client.presenters.entity.PrefixPresenter
import s2js.compiler.javascript
import cz.payola.web.shared.managers.TransformationManager
import cz.payola.web.client.util.UriHashTools

/**
 * Presenter responsible for the logic around running an transformer evaluation.
 * @param elementToDrawIn ID of the element to render view into
 * @param transformerId ID of the transformer which will be run
 */
class TransformerRunner(elementToDrawIn: String, transformerId: String) extends Presenter
{
    var elapsed = 0
    val parentElement = document.getElementById(elementToDrawIn)
    var transformerEvaluationSuccess = new UnitEvent[Transformer, TransformerEvaluationSuccessEventArgs]
    var transformerRunning = false
    var transformerDone = false
    var graphPresenter: GraphPresenter = null
    var successEventHandler: (TransformerEvaluationSuccessEventArgs => Unit) = null
    var evaluationId = ""
    var checkId = ""
    var storeHandler: Boolean = false
    var intervalHandler: Option[Int] = None
    val prefixPresenter = new PrefixPresenter

    private val pollingPeriod = 500

    @javascript("""jQuery(".transformer-controls .btn-success").click();""")
    private def autorun(pluginView: String) {}

    def initialize() {
        blockPage("Loading transformer data...")
        prefixPresenter.initialize

        DomainData.getTransformerById(transformerId) {
            transformer =>
                val uriEvaluationId = UriHashTools.getUriParameter("evaluation")
                if(uriEvaluationId != "") {
                    TransformerRunner.evaluationExists(uriEvaluationId) {exists =>
                            if(exists) skipEvaluationAndLoadFromCache(uriEvaluationId, transformer)
                            else fatalErrorHandler(new PayolaException("The transformer evaluation does not exist."))}
                    {e => fatalErrorHandler(e)}
                } else {
                    createViewAndInit(transformer)
                    unblockPage()

                    if(UriHashTools.getUriParameter("viewPlugin") != "") {
                        autorun(UriHashTools.getUriParameter("viewPlugin"))
                    } else if(!UriHashTools.isAnyParameterInUri() && UriHashTools.getUriHash() != "") {
                        autorun(UriHashTools.getUriHash())
                    }
                }
        } {
            err => fatalErrorHandler(err)
        }
    }

    @javascript(
        """
            var myDate = new Date(d);
            $('#checkInfoLastCheck').append(myDate.toLocaleString());
        """)
    protected def showLastCheck(d: Long) {    }

    @javascript(
        """
            $('#checkInfoLastCheck').html("");
        """)
    protected def clearLastCheck() {    }

    private def createViewAndInit(transformer: Transformer): TransformerRunnerView = {
        val view = new TransformerRunnerView(transformer, prefixPresenter.prefixApplier)
        view.render(parentElement)
        view.tabs.hideTab(1)
        if (transformer.lastCheck==0){
            view.overviewView.controls.checkInfo.text = "Data sources haven't been checked"
            view.overviewView.controls.checkIconTrue.hide()
        }else{
            if(transformer.checked){
                view.overviewView.controls.checkInfo.text = "Data sources checked successfully, last check: "
                view.overviewView.controls.checkIconFalse.hide()
            } else {
                view.overviewView.controls.checkInfo.text = "Data sources checked negatively, last check: "
                view.overviewView.controls.checkIconTrue.hide()
            }
            showLastCheck(transformer.lastCheck)
        }
        successEventHandler = getSuccessEventHandler(transformer, view)
        transformerEvaluationSuccess = new UnitEvent[Transformer, TransformerEvaluationSuccessEventArgs]
        transformerEvaluationSuccess += successEventHandler

        view.overviewView.controls.runBtn.mouseClicked += {
            evt => runButtonClickHandler(view, transformer)
        }

        view.overviewView.controls.checkBtn.mouseClicked += {
            evt => checkButtonClickHandler(view, transformer)
        }

        view
    }

    private def skipEvaluationAndLoadFromCache(uriEvaluationId: String, transformer: Transformer) {
        //render transformer control page
        val view = new TransformerRunnerView(transformer, prefixPresenter.prefixApplier)
        view.render(parentElement)

        successEventHandler = getSuccessEventHandler(transformer, view)
        transformerEvaluationSuccess = new UnitEvent[Transformer, TransformerEvaluationSuccessEventArgs]
        transformerEvaluationSuccess += successEventHandler

        view.overviewView.controls.runBtn.mouseClicked += {
            evt => runButtonClickHandler(view, transformer)
        }

        evaluationId = uriEvaluationId
        transformerDone = true
        view.overviewView.controls.stopButton.setIsEnabled(false)
        intervalHandler.foreach(window.clearInterval(_))

        initReRun(view, transformer)
        window.onunload = null
        view.overviewView.transformerVisualizer.setAllDone()

        //load all transformations and visualize the graph from cache (it is loaded from the view directly by it's transformation)
        transformerEvaluationSuccess.trigger(new TransformerEvaluationSuccessEventArgs(transformer, TransformationManager.allTransformations))
    }

    private def getSuccessEventHandler(transformer: Transformer, view: TransformerRunnerView): (TransformerEvaluationSuccessEventArgs => Unit) = {
        evt: TransformerEvaluationSuccessEventArgs =>
            blockPage("Loading result...")

            transformerDone = true
            transformerRunning = false
            intervalHandler.foreach(window.clearInterval(_))
            view.overviewView.controls.stopButton.setIsEnabled(false)
            view.overviewView.controls.timeoutInfoBar.addCssClass("none")
            view.overviewView.controls.progressBar.setStyleToSuccess()

            preparePresenter(view, evt)

            val downloadButtonView = new DownloadButtonView()
            downloadButtonView.render(graphPresenter.view.toolbar.htmlElement)

            downloadButtonView.rdfDownloadAnchor.mouseClicked += { e =>
                downloadResultAsRDF()
                true
            }

            downloadButtonView.ttlDownloadAnchor.mouseClicked += { e =>
                downloadResultAsTTL()
                true
            }

            view.tabs.showTab(1)
            view.tabs.switchTab(1)

            transformerEvaluationSuccess -= successEventHandler

            unblockPage()
    }

    private def preparePresenter(view: TransformerRunnerView, succEvent: TransformerEvaluationSuccessEventArgs) {

        val viewPlugin = if(!UriHashTools.isAnyParameterInUri() && UriHashTools.getUriHash() != "") {
            UriHashTools.getUriHash()
        } else { UriHashTools.getUriParameter("viewPlugin") }

        getTransformerEvaluationID.foreach(UriHashTools.setUriParameter("evaluation", _)) //this changes the UriHash

        graphPresenter = new GraphPresenter(view.resultsView.htmlElement, prefixPresenter.prefixApplier, getTransformerEvaluationID)
        graphPresenter.initialize()
        graphPresenter.view.setAvailablePlugins(succEvent.availableTransformators, viewPlugin)

        graphPresenter.view.vertexBrowsing += onVertexBrowsing
    }

    private def onVertexBrowsing(e: VertexEventArgs[_]) {
        graphPresenter.onVertexBrowsingDataSource(e)
    }

    private def runButtonClickHandler(view: TransformerRunnerView, transformer: Transformer) = {
        if (!transformerRunning) {
            transformerRunning = true
            blockPage("Starting transformer...")

            uiAdaptTransformerRunning(view, createViewAndInit _, transformer)
            view.overviewView.controls.timeoutInfo.text = "0"

            TransformerRunner.runTransformerById(transformerId, evaluationId) { id =>
                unblockPage()
                elapsed = 0

                intervalHandler = Some(window.setInterval(() => {
                    elapsed += 1
                    view.overviewView.controls.timeoutInfo.text = elapsed.toString
                }, 1000))

                evaluationId = id
                view.overviewView.controls.progressBar.setProgress(0.02)
                schedulePolling(view, transformer)
            } {
                error => fatalErrorHandler(error)
            }

            window.onunload = { _ =>
                onStopClick(view, createViewAndInit, transformer)
            }
        }
        false
    }

    private def checkButtonClickHandler(view: TransformerRunnerView, transformer: Transformer) = {
            clearLastCheck()
            view.overviewView.controls.checkBtn.setIsEnabled(false)
            view.overviewView.controls.checkBtnCaption.text = "Checking..."
            view.overviewView.controls.checkInfo.text = ""
            view.overviewView.controls.checkIconFalse.hide()
            view.overviewView.controls.checkIconTrue.hide()
            TransformerRunner.runCheck(transformerId, checkId, true) { id =>
                checkId = id
                checkPolling(view, transformer)
            } {
                error => fatalErrorHandler(error)
            }
        false
    }

    private def uiAdaptTransformerRunning(view: TransformerRunnerView, initUI: (Transformer) => Unit, transformer: Transformer) {
        view.overviewView.controls.runBtn.setIsEnabled(false)
        view.overviewView.controls.runBtnCaption.text = "Running Transformer..."
        view.overviewView.controls.stopButton.setIsEnabled(true)
        view.overviewView.controls.timeoutInfoBar.removeCssClass("none")
        view.overviewView.controls.stopButton.mouseClicked += { e =>
            onStopClick(view, initUI, transformer)
            false
        }
    }

    private def onStopClick(view: TransformerRunnerView, initUI: (Transformer) => Unit, transformer: Transformer) {
        if (!transformerDone) {
            transformerRunning = false
            transformerDone = false
            intervalHandler.foreach(window.clearInterval(_))
            view.destroy()
            initUI(transformer)
            window.onunload = null
        }
    }

    private def schedulePolling(view: TransformerRunnerView, transformer: Transformer) = {
        window.setTimeout(() => {
            pollingHandler(view, transformer)
        }, pollingPeriod)
    }

    private def checkPolling(view: TransformerRunnerView, transformer: Transformer) = {
        window.setTimeout(() => {
            checkPollingHandler(view, transformer)
        }, pollingPeriod)
    }

    private def getTransformerEvaluationID: Option[String] = {
        val id = evaluationId
        if (id == "") {
            None
        } else {
            Some(id)
        }
    }

    private def downloadResultAs(extension: String) {
        if (getTransformerEvaluationID.isDefined) {
            window.open(
                "/transformer/%s/evaluation/%s/download.%s".format(transformerId, getTransformerEvaluationID.get, extension))
        } else {
            AlertModal.display("Evaluation hasn't finished yet.", "")
        }
    }

    private def downloadResultAsRDF() {
        downloadResultAs("rdf")
    }

    private def downloadResultAsTTL() {
        downloadResultAs("ttl")
    }

    private def pollingHandler(view: TransformerRunnerView, transformer: Transformer) {
        TransformerRunner.getEvaluationState(evaluationId, transformer.id) {
            state =>
                state match {
                    case s: EvaluationInProgress => renderEvaluationProgress(s, view)
                    case s: EvaluationError => evaluationErrorHandler(s, view, transformer)
                    case s: EvaluationCompleted => evaluationCompletedHandler(s, transformer, view)
                    case s: EvaluationTimeout => evaluationTimeout(view, transformer)
                }

                if (state.isInstanceOf[EvaluationInProgress]) {
                    schedulePolling(view, transformer)
                }
        } {
            error => fatalErrorHandler(error)
        }
    }

    private def checkPollingHandler(view: TransformerRunnerView, transformer: Transformer) {
        TransformerRunner.getCheckState(checkId, transformer.id) {
            state =>
                state match {
                    case s: CheckError => checkErrorHandler(s, view, transformer)
                    case s: CheckSuccess => checkSuccessHandler(s, transformer, view)
                }

                if (state.isInstanceOf[CheckInProgress]) {
                    checkPolling(view, transformer)
                }
        } {
            error => fatalErrorHandler(error)
        }
    }

    private def evaluationErrorHandler(error: EvaluationError, view: TransformerRunnerView, transformer: Transformer) {
        view.overviewView.controls.progressBar.setStyleToFailure()
        view.overviewView.controls.progressBar.setActive(false)
        view.overviewView.controls.progressBar.setProgress(1.0)
        transformerDone = true
        view.overviewView.controls.stopButton.setIsEnabled(false)
        intervalHandler.foreach(window.clearInterval(_))

        error.instanceErrors.foreach { err =>
            view.overviewView.transformerVisualizer.setInstanceError(err._1.id, err._2)
        }

        AlertModal.display("Transformer evaluation error", error.error)

        initReRun(view, transformer)
    }

    private def evaluationTimeout(view: TransformerRunnerView, transformer: Transformer) {
        view.overviewView.controls.progressBar.setStyleToFailure()
        view.overviewView.controls.progressBar.setActive(false)
        transformerDone = true
        view.overviewView.controls.stopButton.setIsEnabled(false)
        intervalHandler.foreach(window.clearInterval(_))
        view.overviewView.controls.timeoutInfoBar.hide()

        AlertModal.display("Time out", "The transformer evaluation has timed out.")

        initReRun(view, transformer)
    }

    private def checkErrorHandler(error: CheckError, view: TransformerRunnerView, transformer: Transformer) {
        view.overviewView.controls.checkBtn.setIsEnabled(true)
        view.overviewView.controls.checkInfo.text = ""
        view.overviewView.controls.checkBtnCaption.text = "Check data sources"
        AlertModal.display("Data source check error", error.error)
    }

    private def initReRun(view: TransformerRunnerView, transformer: Transformer) {
        view.overviewView.controls.runBtn.setIsEnabled(true)
        view.overviewView.controls.runBtnCaption.text = "Run Again"
        window.onunload = null

        view.overviewView.controls.runBtn.mouseClicked.clear()
        view.overviewView.controls.runBtn.mouseClicked += { e =>
            view.destroy()

            transformerDone = false
            transformerRunning = false

            val newView = createViewAndInit(transformer)
            runButtonClickHandler(newView, transformer)
        }
        successEventHandler = getSuccessEventHandler(transformer, view)
    }

    private def evaluationCompletedHandler(success: EvaluationCompleted, transformer: Transformer, view: TransformerRunnerView) {
        view.overviewView.controls.progressBar.setStyleToSuccess()
        view.overviewView.controls.progressBar.setProgress(1.0)
        transformerDone = true
        view.overviewView.controls.stopButton.setIsEnabled(false)
        intervalHandler.foreach(window.clearInterval(_))

        initReRun(view, transformer)

        window.onunload = null

        view.overviewView.transformerVisualizer.setAllDone()

        success.instanceErrors.foreach {
            err =>
                view.overviewView.transformerVisualizer.setInstanceError(err._1.id, err._2)
        }

        view.overviewView.controls.runBtn.addCssClass("btn-success")
        view.overviewView.controls.progressBar.setActive(false)

        transformerEvaluationSuccess.trigger(new TransformerEvaluationSuccessEventArgs(transformer, success.availableVisualTransformators))
    }

    private def checkSuccessHandler(success: CheckSuccess, transformer: Transformer, view: TransformerRunnerView) {
        view.overviewView.controls.checkBtn.setIsEnabled(true)
        view.overviewView.controls.checkBtnCaption.text = "Check data sources"
        if(success.result){
            view.overviewView.controls.checkInfo.text = "Data sources are valid with ask queries"
            view.overviewView.controls.checkIconTrue.show()
        } else {
            view.overviewView.controls.checkInfo.text = "Data sources aren't valid with ask queries"
            view.overviewView.controls.checkIconFalse.show()
        }
        TransformerBuilderData.setTransformerChecked(transformerId, success.result) {
            _ =>
        } {
            _ =>
        }
    }

    private def renderEvaluationProgress(progress: EvaluationInProgress, view: TransformerRunnerView) {
        val progressValue = if (progress.value < 0.02) 0.02 else progress.value
        view.overviewView.controls.progressBar.setProgress(progressValue)

        progress.evaluatedInstances.map {
            inst => view.overviewView.transformerVisualizer.setInstanceEvaluated(inst.id)
        }
        progress.errors.map {
            tuple => view.overviewView.transformerVisualizer.setInstanceError(tuple._1.id, tuple._2)
        }
        progress.runningInstances.map {
            inst => view.overviewView.transformerVisualizer.setInstanceRunning(inst._1.id)
        }
    }
}
