package cz.payola.domain.entities.transformers.optimization

import cz.payola.domain.entities.Plugin
import cz.payola.domain.entities.plugins.TransformerPluginInstance

/**
  * A plugin with its instance.
  * @param plugin The plugin.
  * @param instance The instance of the plugin.
  * @tparam A Type of the plugin.
  */
case class TransformerPluginWithInstance[A <: Plugin](plugin: A, instance: TransformerPluginInstance)
{
    //require(instance.plugin == plugin, "The plugin instance must correspond to the plugin.")
}
