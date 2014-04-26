package cz.payola.common

import cz.payola.common.rdf.Graph
import scala.collection.immutable
import cz.payola.common.entities.plugins.TransformerPluginInstance

/**
 * Result of an analysis evaluation.
 */
abstract class TransformerEvaluationState

case class TransformerEvaluationInProgress(value: Double, evaluatedInstances: immutable.Seq[TransformerPluginInstance],
    runningInstances: Seq[(TransformerPluginInstance, Double)],
    errors: Seq[(TransformerPluginInstance, String)]) extends TransformerEvaluationState

/**
 * An error result that is returned when a fatal error occurs during the analysis evaluation.
 * @param error The fatal error object.
 * @param instanceErrors The plugin instances, whose evaluations caused errors with the error objects.
 */
case class TransformerEvaluationError(error: String, instanceErrors: Seq[(TransformerPluginInstance, String)]) extends TransformerEvaluationState

/**
 * A success result that is returned when the output plugin instance returns a graph without errors. It's possible to
 * return the success result even though some plugin instance evaluations may have caused errors.
 * @param outputGraph The output graph returned by the output plugin instance.
 * @param instanceErrors The plugin instances, whose evaluations caused errors with the error objects.
 */
case class TransformerEvaluationSuccess(outputGraph: Graph, instanceErrors: Seq[(TransformerPluginInstance, String)], string: String = "") extends TransformerEvaluationState

/**
 * A success result that is returned when the output plugin instance returns a graph without errors. It's possible to
 * return the success result even though some plugin instance evaluations may have caused errors.
 * @param availableVisualTransformators List of graph transformators that can be used to process the result graph stored
 *                                      in cache
 * @param instanceErrors The plugin instances, whose evaluations caused errors with the error objects.
 */
case class TransformerEvaluationCompleted(availableVisualTransformators: List[String], instanceErrors: Seq[(TransformerPluginInstance, String)]) extends TransformerEvaluationState

/**
 * A result meaning that the analysis evaluation hasn't finished in the specified time limit.
 */
class TransformerEvaluationTimeout extends TransformerEvaluationState

/**
 * A result meaning that the analysis evaluation has been stopped.
 */
class TransformerEvaluationStopped extends TransformerEvaluationState
