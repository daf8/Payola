package cz.payola.common.entities.transformers

import cz.payola.common.Entity
import cz.payola.common.entities.plugins._

/**
 * A binding between the output of one plugin instance and the specified input of another plugin instance.
 */
trait TransformerCompatibilityCheck extends Entity
{
    /** Type of the plugin instances the current binding is between. */
    type TransformerPluginInstanceType <: TransformerPluginInstance

    type DataSourceType <: DataSource

    protected var _sourcePluginInstance: TransformerPluginInstanceType

    protected var _compatibleDataSource: DataSourceType

    override def classNameText = "compatibility check"

    /** The plugin instance that acts as a source of the binding (the binding is connected to its output). */
    def sourcePluginInstance = _sourcePluginInstance

    /** The plugin instance that acts as a target of the binding (the binding is connected to its input). */
    def compatibleDataSource = _compatibleDataSource

}
