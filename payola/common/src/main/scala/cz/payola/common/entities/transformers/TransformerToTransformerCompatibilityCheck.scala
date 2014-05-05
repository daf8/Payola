package cz.payola.common.entities.transformers

import cz.payola.common.Entity
import cz.payola.common.entities.plugins._
import cz.payola.common.entities._

/**
 * A binding between the output of one plugin instance and the specified input of another plugin instance.
 */
trait TransformerToTransformerCompatibilityCheck extends Entity
{
    /** Type of the plugin instances the current binding is between. */
    type TransformerPluginInstanceType <: TransformerPluginInstance

    type TransformerType <: Transformer

    protected var _sourcePluginInstance: TransformerPluginInstanceType

    protected var _compatibleTransformer: TransformerType

    override def classNameText = "compatibility check"

    def sourcePluginInstance = _sourcePluginInstance

    def compatibleTransformer = _compatibleTransformer

}
