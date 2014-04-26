package cz.payola.common.entities.transformers

import cz.payola.common.Entity
import cz.payola.common.entities.plugins.TransformerPluginInstance

/**
 * A binding between the output of one plugin instance and the specified input of another plugin instance.
 */
trait TransformerPluginInstanceBinding extends Entity
{
    /** Type of the plugin instances the current binding is between. */
    type TransformerPluginInstanceType <: TransformerPluginInstance

    protected var _sourcePluginInstance: TransformerPluginInstanceType

    protected var _targetPluginInstance: TransformerPluginInstanceType

    protected val _targetInputIndex: Int

    override def classNameText = "plugin instance binding"

    /** The plugin instance that acts as a source of the binding (the binding is connected to its output). */
    def sourcePluginInstance = _sourcePluginInstance

    /** The plugin instance that acts as a target of the binding (the binding is connected to its input). */
    def targetPluginInstance = _targetPluginInstance

    /** Index of the target plugin instance input the binding is connected to. */
    def targetInputIndex = _targetInputIndex
}
