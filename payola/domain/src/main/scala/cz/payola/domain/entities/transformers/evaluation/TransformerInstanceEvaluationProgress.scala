package cz.payola.domain.entities.transformers.evaluation

import cz.payola.domain.entities.plugins.TransformerPluginInstance

/**
 * A progress of a plugin instance evaluation during a transformer evaluation.
 * @param instance The plugin insatnce.
 * @param value Percentual value of the plugin evaluation progress.
 */
case class TransformerInstanceEvaluationProgress(instance: TransformerPluginInstance, value: Double)
{
    require(instance != null, "The instance mustn't be null.")
    require(value >= 0.0 && value <= 1.0, "The progress value has to be within [0.0, 1.0] interval.")
}
