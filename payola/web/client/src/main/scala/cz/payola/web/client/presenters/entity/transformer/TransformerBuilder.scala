package cz.payola.web.client.presenters.entity.transformer

import s2js.adapters.browser._
import cz.payola.web.client.views.entity.plugins._
import cz.payola.web.client.presenters.components._
import cz.payola.web.shared._
import cz.payola.common.entities._
import scala.collection.mutable.ArrayBuffer
import cz.payola.web.client.presenters.models.ParameterValue
import cz.payola.web.client.events.EventArgs
import cz.payola.web.client.views.bootstrap._
import cz.payola.web.client.Presenter
import cz.payola.web.client.views.entity.transformer.TransformerEditorView
import cz.payola.common.entities.plugins._
import scala.collection.mutable
import cz.payola.web.client.views.bootstrap.modals.AlertModal
import cz.payola.common.ValidationException
import cz.payola.web.client.views.elements.form.fields.TextInput
import s2js.runtime.shared.rpc.RpcException
import cz.payola.web.shared.managers._
import cz.payola.web.client.views.elements.lists.ListItem
import cz.payola.web.client.views.elements._
import scala.Some
import cz.payola.common.rdf.DataCubeVocabulary
import cz.payola.common.rdf.DataCubeDataStructureDefinition
import cz.payola.web.client.presenters.entity.PrefixPresenter
import s2js.compiler.javascript
import cz.payola.domain.entities.plugins.concrete.DataFetcher
import cz.payola.domain._
import cz.payola.domain.rdf.Graph
import cz.payola.common.rdf.DataCubeVocabulary
import cz.payola.common.rdf.DataCubeDataStructureDefinition

/**
 * Presenter responsible for the logic of the Transformer Builder editor.
 * @param parentElementId ID of the DOM element to render views into
 */
class TransformerBuilder(parentElementId: String) extends Presenter
{
    protected val parentElement = document.getElementById(parentElementId)

    protected var allPlugins: Seq[Plugin] = List()

    protected var allSources: Seq[DataSource] = List()

    protected val instancesMap = new mutable.HashMap[String, TransformerPluginInstanceView]

    protected var transformerId = ""

    protected var branches = new ArrayBuffer[TransformerPluginInstanceView]

    protected var instanceViewFactory: PluginInstanceViewFactory = null

    protected val prefixPresenter = new PrefixPresenter

    protected var nameComponent = new InputControl(
        "Transformer name",
        new TextInput("init-name", "", "Enter transformer name"), Some("col-lg-3"), Some("col-lg-9")
    )

    def initialize() {
        prefixPresenter.initialize
        instanceViewFactory = new PluginInstanceViewFactory(prefixPresenter.prefixApplier)

        val nameDialog = new Modal("Please, enter the name of the new transformer", List(nameComponent))
        nameDialog.render()

        nameDialog.confirming += {
            e =>
                nameDialog.block("Initializing transformer...")
                TransformerBuilderData.setTransformerName(transformerId, nameComponent.field.value) {
                    success =>
                        TransformerBuilderData.createEmptyTransformer(nameComponent.field.value) {
                            transformer =>
                                transformerId = transformer.id

                                lockTransformerAndLoadPlugins({
                                    () =>
                                        val view = new
                                                TransformerEditorView(transformer, Some(nameComponent.field.value), None, None,
                                                    "Create transformer", prefixPresenter.prefixApplier)
                                        view.visualizer.pluginInstanceRendered += {
                                            e => instancesMap.put(e.target.pluginInstance.id, e.target)
                                        }
                                        view.render(parentElement)
                                        view.setName(nameComponent.field.value)

                                        view.runButton.mouseClicked += {
                                            args =>
                                                window.location.href = "/transformer/" + transformerId
                                                true
                                        }

                                        bindMenuEvents(view, transformer)
                                        nameDialog.unblock()
                                        nameDialog.destroy()
                                })
                        } {
                            error =>
                                nameDialog.unblock()
                                error match {
                                    case rpc: ValidationException => {
                                        nameComponent.setError(rpc.message)
                                        false
                                    }
                                    case error: RpcException => {
                                        AlertModal.display("Validation failed", error.message)
                                        nameDialog.destroy()
                                    }
                                    case _ => fatalErrorHandler(_)
                                }
                                unblockPage()
                        }
                } {
                    error =>
                        fatalErrorHandler(error)
                        unblockPage()
                }
                false
        }

        nameDialog.closing += {
            e =>
                window.location.href = "/dashboard"
                true
        }
    }

    private def bindInstanceViewActions(instanceView: EditableTransformerPluginInstanceView, view: TransformerEditorView,
        transformer: Transformer) {
        instanceView.connectButtonClicked += {
            evt =>
                connectPlugin(evt.target, view, transformer)
                false
        }
        instanceView.askButtonClicked += onAskClicked(view,transformer)

        instanceView.parameterValueChanged += onParameterValueChanged
        instanceView.deleteButtonClicked += onDeleteClick
    }

    private def onInstanceCreated(createdInstance: TransformerPluginInstance, predecessor: Option[TransformerPluginInstanceView],
        view: TransformerEditorView, transformer: Transformer) {

        val instanceView = instanceViewFactory.transformerCreateEditable(transformer, createdInstance, predecessor.map(List(_)).getOrElse(List()))

        branches.append(instanceView)
        view.visualizer.renderPluginInstanceView(instanceView)
        bindInstanceViewActions(instanceView, view, transformer)

        predecessor.map {
            p =>
                branches -= p
                p.hideControls()
                bind(p, instanceView, 0)
        }
    }

    private def buildDataCubeDataStructuresList(vocabulary: DataCubeVocabulary, predecessor: TransformerPluginInstanceView,
        view: TransformerEditorView, transformer: Transformer) = {
        vocabulary.dataStructureDefinitions.map {
            d =>
                val link = new Anchor(List(new Text(d.uri)), "#", "", d.label)
                link.mouseClicked += {
                    evt =>
                        createDataCubePluginAndInsert(d, predecessor, view, vocabulary.uri, transformer)
                        false
                }
                new ListItem(List(link))
        }
    }

    private def onCreateTransformerPluginClicked(view: TransformerEditorView,
        transformer: Transformer){

        val dialog = new TransformerPluginDialog()

        dialog.confirming += { evtArgs =>
            val transformerId = dialog.getChosenTransformerID
            dialog.destroy()
            blockPage("Cloning the selected transformer")

            DomainData.cloneTransformer(transformerId){ clonedTransformer =>
                unblockPage()
                val transformerDialog = new TransformerParamSelectorDialog(clonedTransformer)
                transformerDialog.confirming += { e =>
                    createTransformerPluginAndInsert(transformerDialog.paramIds, clonedTransformer.id, view, transformer)
                    transformerDialog.destroy()
                    false
                }

                transformerDialog.render()

            } { _ => unblockPage() }
            false
        }

        dialog.render()

    }

    private def onCreateDataCubePluginClicked(predecessor: TransformerPluginInstanceView, view: TransformerEditorView,
        transformer: Transformer) {
        val dialog = new DataCubeDialog()

        var okClicked = false


        dialog.confirming += {
            evtArgs =>
                if (!okClicked) {
                    dialog.block("Parsing the vocabulary definition...")

                    RDFManager.parseDataCubeVocabulary(dialog.dcvUrlField.field.value) {
                        vocabulary =>

                            val list = buildDataCubeDataStructuresList(vocabulary, predecessor, view, transformer)
                            val definitionsDialog = new DataCubeDefinitionsDialog(list)

                            list.map {
                                item =>
                                    item.mouseClicked += {
                                        e =>
                                            definitionsDialog.destroy()
                                            false
                                    }
                            }

                            definitionsDialog.render()

                            dialog.unblock()
                            dialog.destroy()
                    } {
                        e =>
                            dialog.unblock()
                            fatalErrorHandler(e)
                    }

                    okClicked = true
                }
                false
        }

        dialog.render()
    }

    private def createTransformerPluginAndInsert(paramIds: Seq[(String, String)], transformerId: String, view: TransformerEditorView, transformer: Transformer){
        blockPage("Creating the plugin")
        PluginManager.createTransformerInstance(paramIds.map{ t => t._1+":~:"+t._2 }, transformerId){
            plugin => onPluginNameClicked(plugin, None, view, transformer)
        } { _ => unblockPage() }
    }

    private def createDataCubePluginAndInsert(dataStructureDefiniton: DataCubeDataStructureDefinition,
        predecessor: TransformerPluginInstanceView, view: TransformerEditorView, vocabularyUrl: String, transformer: Transformer) {
        PluginManager.createDataCubeInstance(vocabularyUrl, dataStructureDefiniton.uri) {
            plugin =>
                onPluginNameClicked(plugin, Some(predecessor), view, transformer)
        } {
            _ =>
                unblockPage()
        }
    }

    private def onPluginNameClicked(plugin: Plugin, predecessor: Option[TransformerPluginInstanceView],
        view: TransformerEditorView, transformer: Transformer) = {
        blockPage("Creating an instance of the plugin...")

        TransformerBuilderData.createPluginInstance(plugin.id, transformerId) {
            createdInstance =>
                onInstanceCreated(createdInstance, predecessor, view, transformer)
                unblockPage()
        } {
            _ =>
                unblockPage()
        }
    }

    def connectPlugin(pluginInstance: TransformerPluginInstanceView, view: TransformerEditorView, transformer: Transformer): Unit = {
        val inner = pluginInstance

        val dialog = new PluginDialog(allPlugins.filter(_.inputCount == 1))
        dialog.pluginNameClicked += {
            evtArgs =>
                onPluginNameClicked(evtArgs.target, Some(inner), view, transformer)
                dialog.destroy()
                false
        }

        dialog.createDataCubePluginClicked += {
            evtArgs =>
                dialog.destroy()
                onCreateDataCubePluginClicked(pluginInstance, view, transformer)
                false
        }

        dialog.render()
    }

    def onDeleteClick(eventArgs: EventArgs[TransformerPluginInstanceView]) {
        val instance = eventArgs.target
        blockPage("Deleting...")
        TransformerBuilderData.deletePluginInstance(transformerId, instance.pluginInstance.id) {
            _ =>
                branches -= instance
                var i = 0
                while (i < instance.predecessors.size) {
                    branches += instance.predecessors(i)
                    instance.predecessors(i).showControls()
                    i += 1
                }
                instance.destroy()
                unblockPage()
        } {
            _ =>
                unblockPage()
                AlertModal.display("Error when deleting", "The plugin could not be deleted.")
        }
    }

    def bind(a: TransformerPluginInstanceView, b: TransformerPluginInstanceView, inputIndex: Int) {
        blockPage("Working...")
        TransformerBuilderData.saveBinding(transformerId, a.pluginInstance.id, b.pluginInstance.id, inputIndex) {
            _ => unblockPage()
        } {
            e => unblockPage()
                fatalErrorHandler(e)
        }
    }

    protected def onParameterValueChanged(args: EventArgs[ParameterValue]) {
        val parameterInfo = args.target
        parameterInfo.control.isActive = true
        TransformerBuilderData
            .setParameterValue(transformerId, parameterInfo.pluginInstanceId, parameterInfo.name, parameterInfo.value) {
            () =>
                parameterInfo.control.setOk()
                parameterInfo.control.isActive = false
        } {
            e =>
                e match {
                    case ex: ValidationException => {
                        parameterInfo.control.setError("Wrong parameter value.")
                        parameterInfo.control.isActive = false
                    }
                    case _ => fatalErrorHandler(e)
                }
        }
    }

    protected def onConnectClicked(view: TransformerEditorView,
        transformer: Transformer): (EventArgs[TransformerPluginInstanceView]) => Unit = {
        evt =>
            connectPlugin(evt.target, view, transformer)
            false
    }

    protected def onAskClicked(view: TransformerEditorView,
        transformer: Transformer): (EventArgs[TransformerPluginInstanceView]) => Unit = {
        evt =>
            blockPage("Checking data sources with ASK query")
            TransformerBuilderData.removeChecks(transformerId,evt.target.getId){
                r =>
            }{
                err =>
                    fatalErrorHandler(err)
            }
            val x:Int = allSources.count(_ => true)
            var y:Int = 0
            var z:Int = 0
            allSources.map{
                s =>
                    TransformerBuilderData.checkDataSource(transformerId,s.id,evt.target.getId){
                        r =>
                            y=y+1
                            if(r){
                                z=z+1
                            }
                            if (x==y){
                                AlertModal.display("Data source check successful","Available data sources: "+z)
                                unblockPage()
                            }
                    }{
                        err =>
                            fatalErrorHandler(err)
                    }
            }
            false
    }

    protected def lockTransformerAndLoadPlugins(callback: (() => Unit)) {
        TransformerBuilderData.lockTransformer(transformerId) {
            () =>
                TransformerBuilderData.getPlugins() {
                    plugins =>
                        allPlugins = plugins

                        TransformerBuilderData.getDataSources() {
                            sources =>
                                allSources = sources
                                callback()
                        } {
                            error => fatalErrorHandler(error)
                        }
                } {
                    error => fatalErrorHandler(error)
                }
        } {
            error => fatalErrorHandler(error)
        }
    }

    protected def bindNameChanged(view: TransformerEditorView) {
        view.name.delayedChanged += {
            _ =>
                view.name.isActive = true
                TransformerBuilderData.setTransformerName(transformerId, view.name.field.value) {
                    _ =>
                        view.name.isActive = false
                        view.name.setOk()
                } {
                    _ =>
                        view.name.isActive = false
                        view.name.setError("Invalid name.")
                }
        }
    }

    protected def bindDescriptionChanged(view: TransformerEditorView) {
        view.description.delayedChanged += {
            _ =>
                view.description.isActive = true
                TransformerBuilderData.setTransformerDescription(transformerId, view.description.field.value) {
                    _ =>
                        view.description.isActive = false
                        view.description.setOk()
                } {
                    _ =>
                        view.description.isActive = false
                        view.description.setError("Invalid description.")
                }
        }
    }

    protected def bindTtlChanged(view: TransformerEditorView) {
        view.ttl.delayedChanged += {
            _ =>
                view.ttl.isActive = true
                TransformerBuilderData.setTransformerTtl(transformerId, view.ttl.field.value) {
                    _ =>
                        view.ttl.isActive = false
                        view.ttl.setOk()
                } {
                    _ =>
                        view.ttl.isActive = false
                        view.ttl.setError("Invalid turtle data.")
                }
        }
    }

    protected def bindTtlFileTtlChanged(view: TransformerEditorView) {
        view.ttlFileInput.delayedChanged += {
            _ =>
                view.ttl.isActive = true
                TransformerBuilderData.setTransformerTtl(transformerId, view.ttl.field.value) {
                    _ =>
                        view.ttl.isActive = false
                        view.ttl.setOk()
                } {
                    _ =>
                        view.ttl.isActive = false
                        view.ttl.setError("Invalid turtle data.")
                }
        }
    }

    @javascript(
        """
            function readSingleFile(evt) {
              var f = evt.target.files[0];
              if (f) {
                var r = new FileReader();
          		r.onload = (function(theFile) {
                  return function(e) {
                    $('#ttl').val(e.target.result);
                  };
                })(f);
                r.readAsText(f);
              } else {
                alert("Failed to load file");
              }
            }

            $('#ttlFile').change(readSingleFile);
        """)
    protected def bindTtlFileChanged(view: TransformerEditorView) {    }

    protected def bindAddPluginClicked(view: TransformerEditorView, transformer: Transformer) {
        view.addPluginLink.mouseClicked += {
            _ =>
                blockPage("Checking available plugins...")
                TransformerBuilderData.getTransformer(transformerId) {
                    t =>
                        if(t.pluginInstances.length==0) {
                            val dialog = new
                                    PluginDialog(
                                        allPlugins.filter(_.inputCount == 0).filterNot(_.name == "Payola Private Storage"))
                            dialog.pluginNameClicked += {
                                evtArgs =>
                                    onPluginNameClicked(evtArgs.target, None, view, transformer)
                                    dialog.destroy()
                                    false
                            }

                            dialog.createTransformerPluginClicked += {
                                evtArgs =>
                                    dialog.destroy()
                                    onCreateTransformerPluginClicked(view, transformer)
                                    false
                            }

                            dialog.render()
                        } else {
                            AlertModal.display("Transformer can have only one input", "To add another plugin into the pipeline press 'Add Connection' button at the last plugin.")
                        }
                        unblockPage()
                }{
                    err =>
                        fatalErrorHandler(err)
                }
                false
        }
    }

    protected def bindMenuEvents(view: TransformerEditorView, transformer: Transformer) {
        bindDescriptionChanged(view)
        bindTtlChanged(view)
        bindTtlFileChanged(view)
        bindTtlFileTtlChanged(view)
        bindNameChanged(view)
        bindAddPluginClicked(view, transformer)
    }
}
