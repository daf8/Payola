package cz.payola.domain.entities.transformers.evaluation

/**
 * A message used to control an analysis evaluation.
 */
private[evaluation] abstract class TransformerEvaluationControl

/**
 * Signal to stop the analysis evaluation.
 */
private[evaluation] object Stop extends TransformerEvaluationControl

/**
 * Signal to terminate the analysis evaluation.
 */
private[evaluation] object Terminate extends TransformerEvaluationControl

/**
 * Signal to respond with the analysis evaluation progress.
 */
private[evaluation] object GetProgress extends TransformerEvaluationControl

/**
 * Signal to respond with the analysis evaluation result.
 */
private[evaluation] object GetResult extends TransformerEvaluationControl
