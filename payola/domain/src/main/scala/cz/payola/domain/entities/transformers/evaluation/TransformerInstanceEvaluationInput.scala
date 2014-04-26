package cz.payola.domain.entities.transformers.evaluation

import cz.payola.domain.rdf.Graph

/**
 * A single input of a plugin instance during analysis evaluation.
 * @param index Index of the input.
 * @param value The input graph.
 */
case class TransformerInstanceEvaluationInput(index: Int, value: Option[Graph])
