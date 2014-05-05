package cz.payola.data.squeryl.entities

import cz.payola.data.squeryl.entities.transformers._
import cz.payola.data.squeryl.entities.plugins.TransformerPluginInstance
import scala.collection.immutable
import scala.collection.mutable
import cz.payola.data.squeryl._

/**
 * This object converts [[cz.payola.common.entities.Transformer]] to [[cz.payola.data.squeryl.entities.Transformer]]
 */
object Transformer extends EntityConverter[Transformer]
{
    def convert(entity: AnyRef)(implicit context: SquerylDataContextComponent): Option[Transformer] = {
        entity match {
            case e: Transformer => Some(e)
            case e: cz.payola.common.entities.Transformer
            => Some(new Transformer(e.id, e.name, e.owner.map(User(_)), e.isPublic, e.description, e.token, e.isVisibleInListings))
            case _ => None
        }
    }
}

/**
 * Provides database persistence to [[cz.payola.domain.entities.Transformer]] entities.
 * @param id ID of the Transformer
 * @param name Name of the Transformer
 * @param o Owner of the Transformer
 * @param _isPub Whether the Transformer is public or not
 * @param _desc Description of the Transformer
 * @param context Implicit context
 */
class Transformer(override val id: String, name: String, o: Option[User], var _isPub: Boolean, var _desc: String, var _token: Option[String], var __isVisibleInListings: Boolean)
    (implicit val context: SquerylDataContextComponent)
    extends cz.payola.domain.entities.Transformer(name, o)
    with Entity with OptionallyOwnedEntity with ShareableEntity with DescribedEntity
{
    type DomainParameterValueType = plugins.ParameterValue[_]

    _pluginInstances = null

    _isVisibleInListings = __isVisibleInListings

    private lazy val _pluginInstancesQuery = context.schema.transformersPluginInstances.left(this)

    _pluginInstanceBindings = null

    private lazy val _pluginInstancesBindingsQuery = context.schema.transformersPluginInstancesBindings.left(this)

    _compatibilityChecks = null

    private lazy val _compatibilityCheckQuery = context.schema.transformersCompatibilityChecks.left(this)

    _compatibilityTransformerChecks = null

    private lazy val _compatibilityTransformerCheckQuery = context.schema.transformersToTransformerCompatibilityChecks.left(this)

    _defaultCustomization = null

    var defaultCustomizationId: Option[String] = None

    token = _token

    override def defaultOntologyCustomization_=(value: Option[Transformer#OntologyCustomizationType]) {
        defaultCustomizationId = value.map(_.id)

        // Save
        context.transformerRepository.persist(this)

        super.defaultOntologyCustomization = value
    }

    override def defaultOntologyCustomization = {
        if (_defaultCustomization == null) {
            context.transformerRepository.loadDefaultOntology(this)
        }

        _defaultCustomization
    }

    override def pluginInstances: immutable.Seq[TransformerPluginInstanceType] = {
        if (_pluginInstances == null) {
            context.transformerRepository.loadPluginInstances(this)
        }

        _pluginInstances.toList
    }

    def pluginInstances_=(value: Seq[TransformerPluginInstanceType]) {
        _pluginInstances = mutable.ArrayBuffer(value: _*)
    }

    override def pluginInstanceBindings: immutable.Seq[TransformerPluginInstanceBindingType] = {
        if (_pluginInstanceBindings == null) {
            context.transformerRepository.loadPluginInstanceBindings(this)
        }

        _pluginInstanceBindings.toList
    }

    def pluginInstanceBindings_=(value: Seq[TransformerPluginInstanceBindingType]) {
        _pluginInstanceBindings = mutable.ArrayBuffer(value: _*)
    }

    override def compatibilityChecks: immutable.Seq[TransformerCompatibilityCheckType] = {
        if (_compatibilityChecks == null) {
            context.transformerRepository.loadCompatibilityChecks(this)
        }

        _compatibilityChecks.toList
    }

    def compatibilityChecks_=(value: Seq[TransformerCompatibilityCheckType]) {
        _compatibilityChecks = mutable.ArrayBuffer(value: _*)
    }

    override def compatibilityTransformerChecks: immutable.Seq[TransformerToTransformerCompatibilityCheckType] = {
        if (_compatibilityTransformerChecks == null) {
            context.transformerRepository.loadCompatibilityTransformerChecks(this)
        }

        _compatibilityTransformerChecks.toList
    }

    def compatibilityTransformerChecks_=(value: Seq[TransformerToTransformerCompatibilityCheckType]) {
        _compatibilityTransformerChecks = mutable.ArrayBuffer(value: _*)
    }

    override protected def storePluginInstance(instance: Transformer#TransformerPluginInstanceType) {
        super.storePluginInstance(associatePluginInstance(TransformerPluginInstance(instance)))
    }

    override protected def discardPluginInstance(instance: Transformer#TransformerPluginInstanceType) {
        context.transformerRepository.removePluginInstanceById(instance.id)

        super.discardPluginInstance(instance)
    }

    override protected def storeBinding(binding: Transformer#TransformerPluginInstanceBindingType) {
        super.storeBinding(associatePluginInstanceBinding(TransformerPluginInstanceBinding(binding)))
    }

    override protected def storeChecking(checking: Transformer#TransformerCompatibilityCheckType) {
        super.storeChecking(associateCompatibilityCheck(TransformerCompatibilityCheck(checking)))
    }

    override protected def storeTransformerChecking(checking: Transformer#TransformerToTransformerCompatibilityCheckType) {
        super.storeTransformerChecking(associateTransformerCompatibilityCheck(TransformerToTransformerCompatibilityCheck(checking)))
    }

    override protected def discardBinding(binding: Transformer#TransformerPluginInstanceBindingType) {
        context.transformerRepository.removePluginInstanceBindingById(binding.id)

        super.discardBinding(binding)
    }

    override protected def discardChecking(checking: Transformer#TransformerCompatibilityCheckType) {
        context.transformerRepository.removeCompatibilityCheckById(checking.id)

        super.discardChecking(checking)
    }

    override protected def discardTransformerChecking(checking: Transformer#TransformerToTransformerCompatibilityCheckType) {
        context.transformerRepository.removeCompatibilityTransformerCheckById(checking.id)

        super.discardTransformerChecking(checking)
    }

    def associatePluginInstance(instance: TransformerPluginInstance): TransformerPluginInstance = {
        context.schema.associate(instance, _pluginInstancesQuery)
        context.transformerRepository.persistPluginInstance(instance)

        instance
    }

    def associatePluginInstanceBinding(instance: TransformerPluginInstanceBinding): TransformerPluginInstanceBinding = {
        context.schema.associate(instance, _pluginInstancesBindingsQuery)
    }

    def associateCompatibilityCheck(instance: TransformerCompatibilityCheck): TransformerCompatibilityCheck = {
        context.schema.associate(instance, _compatibilityCheckQuery)
    }

    def associateTransformerCompatibilityCheck(instance: TransformerToTransformerCompatibilityCheck): TransformerToTransformerCompatibilityCheck = {
        context.schema.associate(instance, _compatibilityTransformerCheckQuery)
    }
}