package cz.payola.domain.entities

import scala.collection.mutable
import cz.payola.domain.entities.transformers._
import cz.payola.domain.entities.transformers.evaluation.TransformerEvaluation
import plugins._
import cz.payola.domain.Entity
import cz.payola.domain.entities.settings.OntologyCustomization
import cz.payola.domain.entities.plugins.concrete._
import parameters._
import scala.Some
import scala.Some

/**
 * @param _name Name of the transformer.
 * @param _owner Owner of the transformer.
 */
class Transformer(protected var _name: String, protected var _owner: Option[User])
    extends Entity
    with NamedEntity
    with OptionallyOwnedEntity
    with cz.payola.common.entities.Transformer
{
    checkConstructorPostConditions()

    type TransformerPluginInstanceType = TransformerPluginInstance

    type TransformerPluginInstanceBindingType = TransformerPluginInstanceBinding

    type TransformerCompatibilityCheckType = TransformerCompatibilityCheck

    type TransformerToTransformerCompatibilityCheckType = TransformerToTransformerCompatibilityCheck

    type InstanceBindings = Map[TransformerPluginInstance, Seq[TransformerPluginInstanceBinding]]

    type OntologyCustomizationType = OntologyCustomization

    protected var _pluginInstanceInputBindings: Option[InstanceBindings] = None

    protected var _pluginInstanceOutputBindings: Option[InstanceBindings] = None

    /**
     * Starts evaluation of the transformer.
     * @param timeout Maximal execution time in milliseconds.
     * @return An instance of the [[cz.payola.domain.entities.analyses.evaluation.TransformerEvaluation]] which can be
     *         queried about the transformer evaluation progress and the result.
     */
    def evaluate(timeout: Option[Long] = None): TransformerEvaluation = {
        val evaluation = new TransformerEvaluation(this, timeout)
        evaluation.start()
        evaluation
    }

    def expand(accessibleTransformers: Seq[Transformer]) {
        pluginInstances.foreach{ i =>
            i.plugin match {
                case a : TransformerPlugin => {
                    val transformerId = i.getStringParameter("Transformer ID")

                    val remappedParamValues = new mutable.HashMap[String, ParameterValue[_]]()
                    i.parameterValues.filter(_.parameter.name.contains("$")).foreach { p =>
                        remappedParamValues += (p.parameter.name.split("""\$""").apply(1) -> p)
                    }

                    def remapParams {
                        _pluginInstances.foreach{pi =>
                            pi.parameterValues.map { pv =>
                                remappedParamValues.get(pv.id).foreach{ paramVal =>

                                    (pv, paramVal) match {
                                        case (o: StringParameterValue, n: StringParameterValue) => o.value = n.value
                                        case (o: IntParameterValue, n: IntParameterValue) => o.value = n.value
                                        case (o: BooleanParameterValue, n: BooleanParameterValue) => o.value = n.value
                                        case (o: FloatParameterValue, n: FloatParameterValue) => o.value = n.value
                                        case _ =>
                                    }

                                }
                            }
                        }
                    }

                    transformerId.map { idParam =>
                        accessibleTransformers.find(_.id == idParam).map { transformer =>

                            remapParams

                            transformer.expand(accessibleTransformers)

                            _pluginInstances ++= transformer.pluginInstances
                            _pluginInstanceBindings ++= transformer.pluginInstanceBindings

                            pluginInstanceBindings.find(_.sourcePluginInstance == i).map { b =>

                                transformer.outputInstance.map{ o =>
                                    _pluginInstanceBindings ++= Seq(new TransformerPluginInstanceBinding(o, b.targetPluginInstance, b.targetInputIndex))
                                }
                            }

                            remapParams
                        }
                    }

                    _pluginInstanceBindings --= pluginInstanceBindings.filter(_.sourcePluginInstance == i)
                    _pluginInstances --= Seq(i)
                }
                case _ =>
            }
        }
    }

    /**
     * Checks whether the transformer is valid (i.e. it can be evaluated). If not, then
     * [[cz.payola.domain.entities.analyses.AnalysisException]] is thrown.
     */
    def checkValidity()  {
        if (pluginInstances.isEmpty) {
            throw new TransformerException("The transformer is empty.")
        }

        // Check input bindings.
        val instanceInvalidBindings = pluginInstanceInputBindings.filter {instanceBindings =>
            val instance = instanceBindings._1
            val bindings = instanceBindings._2
            val x = bindings.map(_.targetInputIndex).distinct
            instance.plugin.inputCount != bindings.length || // Number of input bindings isn't input count.
                bindings.length != x.length // Input has more than one binding.
        }
        if (instanceInvalidBindings.nonEmpty) {
            throw new TransformerException("The transformer contains plugin instances with invalid input bindings.")
        }

        // Check output bindings.
        if (pluginInstanceOutputBindings.exists(_._2.length > 1)) {
            throw new TransformerException("The transformer contains plugin instances with invalid output bindings.")
        }

        // Check transformer output.
        val outputInstances = pluginInstanceOutputBindings.filter(_._2.isEmpty).map(_._1).toList
        if (outputInstances.length != 1) {
            throw new TransformerException("The transformer doesn't contain one output.")
        }
        val outputInstance = outputInstances.head

        // Check cycles.
        val visitedInstances = new mutable.ArrayBuffer[TransformerPluginInstance]()
        def visitInstance(instance: TransformerPluginInstance) {
            if (visitedInstances.contains(instance)) {
                throw new TransformerException("The plugin instance bindings contain a cycle.")
            }
            visitedInstances += instance
            pluginInstanceInputBindings(instance).foreach(binding => visitInstance(binding.sourcePluginInstance))
        }
        visitInstance(outputInstance)

        // Check whether the instance graph is connected.
        if (visitedInstances.length != pluginInstances.length) {
            throw new TransformerException("The transformer contains more than one connected plugin instance component.")
        }

        // Check whether there is a DataFetcher connected to a non SPARQL query plugin.
        val invalidBinding = pluginInstanceBindings.find { b =>
            b.sourcePluginInstance.plugin.isInstanceOf[DataFetcher] &&
                !b.targetPluginInstance.plugin.isInstanceOf[SparqlQuery]
        }
        invalidBinding.foreach { b =>
            throw new TransformerException(("The transformer contains a data fetcher plugin (%s) that is directly " +
                "connected to a non SPARQL query plugin (%s). That kind of connection isn't currently supported, " +
                "because it would cause a selection of everything from the storage corresponding to the data " +
                "fetcher.").format(b.sourcePluginInstance.plugin.name, b.targetPluginInstance.plugin.name))
        }
    }

    /**
     * Returns the plugin instance bindings grouped by the target instances (the instances they go to).
     */
    def pluginInstanceInputBindings: InstanceBindings = {
        if (_pluginInstanceInputBindings.isEmpty) {
            _pluginInstanceInputBindings = Some(groupBindingsByInstance(_.targetPluginInstance))
        }
        _pluginInstanceInputBindings.get
    }

    /**
     * Returns the plugin instance bindings grouped by the source instances (the instances they come from).
     */
    def pluginInstanceOutputBindings: InstanceBindings = {
        if (_pluginInstanceOutputBindings.isEmpty) {
            _pluginInstanceOutputBindings = Some(groupBindingsByInstance(_.sourcePluginInstance))
        }
        _pluginInstanceOutputBindings.get
    }

    /**
     * Returns the plugin instances that behave as sources of the transformer (they have no inputs).
     */
    def sourceInstances: Seq[TransformerPluginInstance] = getInstancesWithoutBindings(pluginInstanceInputBindings)

    /**
     * Returns the plugin instances that behave as outputs of the transformer (they don't have their outputs bound).
     */
    def outputInstances: Seq[TransformerPluginInstance] = getInstancesWithoutBindings(pluginInstanceOutputBindings)

    /**
     * Returns the plugin instance whose output is also output of the transformer. If the transformer is valid then
     * [[scala.Some]] is returned.
     */
    def outputInstance: Option[TransformerPluginInstance] = outputInstances.headOption

    /**
     * Adds a new plugin instance to the transformer.
     * @param instance The plugin instance to add.
     * @throws IllegalArgumentException if the instance can't be added to the transformer.
     */
    def addPluginInstance(instance: TransformerPluginInstanceType) {
        require(!pluginInstances.contains(instance), "The instance is already present in the transformer.")

        invalidatePluginInstanceBindings()
        storePluginInstance(instance)
    }

    /**
     * Adds the specified plugin instances to the transformer.
     * @param instances The plugin instance to add.
     */
    def addPluginInstances(instances: TransformerPluginInstanceType*) {
        instances.foreach(i => addPluginInstance(i))
    }

    /**
     * Removes the specified plugin instance and all bindings connected to it from the transformer.
     * @param instance The plugin instance to be removed.
     * @return The removed plugin instance.
     */
    def removePluginInstance(instance: TransformerPluginInstanceType): Option[TransformerPluginInstanceType] = {
        ifContains(pluginInstances, instance) {
            invalidatePluginInstanceBindings()
            val bindingsToRemove = pluginInstanceBindings.filter { binding =>
                binding.sourcePluginInstance == instance || binding.targetPluginInstance == instance
            }
            removeBindings(bindingsToRemove: _*)
            discardPluginInstance(instance)
        }
    }

    /**
     * Removes the specified plugin instances from the transformer.
     * @param instances The plugin instances to remove.
     */
    def removePluginInstances(instances: TransformerPluginInstanceType*) {
        instances.foreach(i => removePluginInstance(i))
    }

    /**
     * Adds a new plugin instance binding to the transformer.
     * @param binding The plugin instance binding to add.
     */
    def addBinding(binding: TransformerPluginInstanceBindingType) {
        require(!pluginInstanceBindings.contains(binding), "The binding is already present in the transformer.")
        require(pluginInstances.contains(binding.sourcePluginInstance),
            "The source plugin instance has to be present in the transformer.")
        require(pluginInstances.contains(binding.targetPluginInstance),
            "The target plugin instance has to be present in the transformer.")

        invalidatePluginInstanceBindings()
        storeBinding(binding)
    }

    /**
     * Adds a new plugin instance binding to the transformer.
     * @param sourcePluginInstance The source plugin instance.
     * @param targetPluginInstance The target plugin instance.
     * @param inputIndex Index of the target plugin instance input the binding is connected to.
     */
    def addBinding(sourcePluginInstance: TransformerPluginInstance, targetPluginInstance: TransformerPluginInstance, inputIndex: Int = 0) {
        addBinding(new TransformerPluginInstanceBinding(sourcePluginInstance, targetPluginInstance, inputIndex))
    }

    /**
     * Adds a new compatibility check to the transformer.
     * @param checking The plugin instance binding to add.
     */
    def addChecking(checking: TransformerCompatibilityCheckType) {
        storeChecking(checking)
    }

    /**
     * Adds a new compatible plugin instance and data source.
     * @param sourcePluginInstance The source plugin instance.
     * @param compatibleAnalysis The compatible data source.
     */
    def addChecking(sourcePluginInstance: TransformerPluginInstance, compatibleAnalysis: Analysis) {
        addChecking(new TransformerCompatibilityCheck(sourcePluginInstance, compatibleAnalysis))
    }

    /**
     * Adds a new compatibility check to the transformer.
     * @param checking The plugin instance binding to add.
     */
    def addTransformerChecking(checking: TransformerToTransformerCompatibilityCheckType) {
        storeTransformerChecking(checking)
    }

    /**
     * Adds a new compatible plugin instance and data source.
     * @param sourcePluginInstance The source plugin instance.
     * @param compatibleTransformer The compatible data source.
     */
    def addTransformerChecking(sourcePluginInstance: TransformerPluginInstance, compatibleTransformer: Transformer) {
        addTransformerChecking(new TransformerToTransformerCompatibilityCheck(sourcePluginInstance, compatibleTransformer))
    }

    /**
     * Collapses the specified binding including the source and the target into one plugin instance. The binding
     * target instance must have exactly one input in order to collapse the binding. The binding source instance must
     * have exactly same number of inputs as the instance that replaces the binding.
     * @param binding The binding to collapse.
     * @param instance The instance that would replace the binding.
     */
    def collapseBinding(binding: TransformerPluginInstanceBindingType, instance: TransformerPluginInstance) {
        require(pluginInstanceBindings.contains(binding), "The binding isn't present in the transformer.")
        require(!pluginInstances.contains(instance), "The instance is already present in the transformer.")
        require(binding.targetPluginInstance.plugin.inputCount == 1, "The binding target instance must have one imput.")
        require(binding.sourcePluginInstance.plugin.inputCount == instance.plugin.inputCount,
            "The binding source instance must have same number of inputs as the instance that replaces the binding.")

        // Store the bindings for later use.
        val sourceInstanceInputBindings = pluginInstanceInputBindings(binding.sourcePluginInstance)
        val targetInstanceOutputBindings = pluginInstanceOutputBindings(binding.targetPluginInstance)

        // Remove the old instances, including the bindings around them.
        removePluginInstances(binding.sourcePluginInstance, binding.targetPluginInstance)

        // Add the new instance and restore the bindings.
        addPluginInstance(instance)
        sourceInstanceInputBindings.foreach(b => addBinding(b.sourcePluginInstance, instance, b.targetInputIndex))
        targetInstanceOutputBindings.foreach(b => addBinding(instance, b.targetPluginInstance, b.targetInputIndex))
    }

    /**
     * Removes the specified plugin instance binding from the transformer.
     * @param binding The plugin instance binding to be removed.
     * @return The removed plugin instance binding.
     */
    def removeBinding(binding: TransformerPluginInstanceBindingType): Option[TransformerPluginInstanceBindingType] = {
        ifContains(pluginInstanceBindings, binding) {
            invalidatePluginInstanceBindings()
            discardBinding(binding)
        }
    }

    /**
     * Removes the specified compatibility check from the transformer.
     * @param checking The compatibility check to be removed.
     * @return The removed compatibility check.
     */
    def removeChecking(checking: TransformerCompatibilityCheckType): Option[TransformerCompatibilityCheckType] = {
        ifContains(compatibilityChecks, checking) {
            discardChecking(checking)
        }
    }

    /**
     * Removes the specified compatibility check from the transformer.
     * @param checking The compatibility check to be removed.
     * @return The removed compatibility check.
     */
    def removeTransformerChecking(checking: TransformerToTransformerCompatibilityCheckType): Option[TransformerToTransformerCompatibilityCheckType] = {
        ifContains(compatibilityTransformerChecks, checking) {
            discardTransformerChecking(checking)
        }
    }

    /**
     * Removes the specified plugin instance bindings from the transformer.
     * @param bindings The plugin instance bindings to be removed.
     */
    def removeBindings(bindings: TransformerPluginInstanceBindingType*) {
        bindings.foreach(removeBinding(_))
    }

    override def canEqual(other: Any): Boolean = {
        other.isInstanceOf[Transformer]
    }

    override protected def checkInvariants() {
        super[Entity].checkInvariants()
        super[NamedEntity].checkInvariants()
        super[OptionallyOwnedEntity].checkInvariants()
    }

    /**
     * Groups the plugin instance bindings to groups indexed by plugin instances.
     * @param f A function that selects the plugin instance, to whose group the plugin instance binding should belong.
     * @return The plugin instance bindings.
     */
    private def groupBindingsByInstance(f: TransformerPluginInstanceBinding => TransformerPluginInstance): InstanceBindings = {
        val instanceBindings = pluginInstanceBindings.groupBy(f)
        val instancesWithoutBindings = pluginInstances.filter(i => !instanceBindings.contains(i))
        instanceBindings ++ instancesWithoutBindings.map((_, Nil)).toMap
    }

    /**
     * Returns plugin instances with no bindings in the specified map of instance bindings.
     * @param instanceBindings The bindings indexed by the plugin instances.
     * @return The plugin instances without bindings.
     */
    private def getInstancesWithoutBindings(instanceBindings: InstanceBindings): Seq[TransformerPluginInstance] = {
        instanceBindings.filter(_._2.isEmpty).map(_._1).toList
    }

    /**
     * Invalidates the plugin instance bindings. Should be called whenever the collection of plugins or plugin
     * instances is altered.
     */
    private def invalidatePluginInstanceBindings() {
        _pluginInstanceInputBindings = None
        _pluginInstanceOutputBindings = None
    }
}