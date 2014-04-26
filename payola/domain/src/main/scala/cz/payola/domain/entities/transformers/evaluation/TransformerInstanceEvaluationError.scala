package cz.payola.domain.entities.transformers.evaluation

import cz.payola.domain.entities.plugins.TransformerPluginInstance

/**
 * An error of the plugin instance evaluation during an analysis evaluation.
 * @param instance The plugin instance.
 * @param throwable The error that occurred.
 */
case class TransformerInstanceEvaluationError(instance: TransformerPluginInstance, throwable: Throwable)
