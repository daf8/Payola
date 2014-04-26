package cz.payola.common.entities

import scala.collection._
import cz.payola.common.Entity
import cz.payola.common.entities.plugins.TransformerPluginInstance
import cz.payola.common.entities.transformers._

/**
 * A set of analytical plugin instances that are bound together (the output of one plugin instance is bound to the
 * input of another plugin instance). The transformer is in a valid state iff all plugin instances have all inputs and
 * outputs bound, no input nor output is bound more than once and there is one plugin instance that doesn't have its
 * output bound. That is the transformer output. If the transformer is in the valid state, it may be evaluated.
 */
trait Transformer extends Entity with NamedEntity with OptionallyOwnedEntity with ShareableEntity with DescribedEntity with TtlEntity with CheckedEntity
{
    /** Type of the analytical plugin instances the transformer consists of. */
    type TransformerPluginInstanceType <: TransformerPluginInstance

    /** Type of the bindings between analytical plugin instances. */
    type TransformerPluginInstanceBindingType <: TransformerPluginInstanceBinding

    /** Type of the compatibility check between plugin instance and data source. */
    type TransformerCompatibilityCheckType <: TransformerCompatibilityCheck

    /** Type of the ontology customization for transformer */
    type OntologyCustomizationType <: settings.OntologyCustomization

    protected var _pluginInstances = mutable.ArrayBuffer[TransformerPluginInstanceType]()

    protected var _pluginInstanceBindings = mutable.ArrayBuffer[TransformerPluginInstanceBindingType]()

    protected var _compatibilityChecks = mutable.ArrayBuffer[TransformerCompatibilityCheckType]()

    protected var _defaultCustomization: Option[OntologyCustomizationType] = None

    var token: Option[String] = None

    /** Analytical plugin instances the transformer consists of.*/
    def pluginInstances: immutable.Seq[TransformerPluginInstanceType] = _pluginInstances.toList

    /** Bindings between the analytical plugin instances. */
    def pluginInstanceBindings: immutable.Seq[TransformerPluginInstanceBindingType] = _pluginInstanceBindings.toList

    /** Bindings between the analytical plugin instances. */
    def compatibilityChecks: immutable.Seq[TransformerCompatibilityCheckType] = _compatibilityChecks.toList

    /** Default ontology customization for this transformer */
    def defaultOntologyCustomization = _defaultCustomization

    /**
     * Sets new default ontology customization for this transformer
     * @param value New default ontology customization for this transformer
     */
    def defaultOntologyCustomization_=(value: Option[OntologyCustomizationType]) { _defaultCustomization = value }

    /**
     * Stores the specified plugin instance to the transformer.
     * @param instance The plugin instance to store.
     */
    protected def storePluginInstance(instance: TransformerPluginInstanceType) {
        _pluginInstances += instance
    }

    /**
     * Discards the specified plugin instance from the transformer. Complementary operation to store.
     * @param instance The plugin instance to discard.
     */
    protected def discardPluginInstance(instance: TransformerPluginInstanceType) {
        _pluginInstances -= instance
    }

    /**
     * Stores the specified plugin instance binding to the transformer.
     * @param binding The plugin instance binding to store.
     */
    protected def storeBinding(binding: TransformerPluginInstanceBindingType) {
        _pluginInstanceBindings += binding
    }

    /**
     * Stores the specified check to the transformer.
     * @param checking Plugin instance compatibility with data source
     */
    protected def storeChecking(checking: TransformerCompatibilityCheckType) {
        _compatibilityChecks += checking
    }

    /**
     * Discards the specified plugin instance from the transformer. Complementary operation to store.
     * @param binding The plugin instance binding to discard.
     */
    protected def discardBinding(binding: TransformerPluginInstanceBindingType) {
        _pluginInstanceBindings -= binding
    }

    /**
     * Discards the specified compatibility check from the transformer. Complementary operation to store.
     * @param checking The compatibility check to discard.
     */
    protected def discardChecking(checking: TransformerCompatibilityCheckType) {
        _compatibilityChecks -= checking
    }
}