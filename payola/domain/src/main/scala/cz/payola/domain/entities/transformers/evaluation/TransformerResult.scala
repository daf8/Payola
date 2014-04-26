package cz.payola.domain.entities.transformers.evaluation

import cz.payola.domain.rdf.Graph
import cz.payola.domain.entities.plugins.TransformerPluginInstance

/**
 * Result of an analysis evaluation.
 */
abstract class TransformerResult

/**
 * An error result that is returned when a fatal error occurs during the analysis evaluation.
 * @param error The fatal error object.
 * @param instanceErrors The plugin instances, whose evaluations caused errors with the error objects.
 */
case class Error(error: Throwable, instanceErrors: Map[TransformerPluginInstance, Throwable]) extends TransformerResult

/**
 * A success result that is returned when the output plugin instance returns a graph without errors. It's possible to
 * return the success result even though some plugin instance evaluations may have caused errors.
 * @param outputGraph The output graph returned by the output plugin instance.
 * @param instanceErrors The plugin instances, whose evaluations caused errors with the error objects.
 */
case class Success(outputGraph: Graph, instanceErrors: Map[TransformerPluginInstance, Throwable]) extends TransformerResult

/**
 * A result meaning that the analysis evaluation hasn't finished in the specified time limit.
 */
object Timeout extends TransformerResult

/**
 * A result meaning that the analysis evaluation has been stopped.
 */
object Stopped extends TransformerResult
