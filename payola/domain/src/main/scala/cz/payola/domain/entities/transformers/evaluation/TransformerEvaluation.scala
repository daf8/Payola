package cz.payola.domain.entities.transformers.evaluation

import actors.{TIMEOUT, Actor}
import collection.mutable
import cz.payola.domain.actors.Timer
import cz.payola.domain.entities.Transformer
import cz.payola.domain.entities.transformers._
import cz.payola.domain.entities.plugins.TransformerPluginInstance
import cz.payola.domain.entities.transformers.optimization._
import cz.payola.domain.rdf.Graph
import cz.payola.domain.entities.transformers.optimization.phases._

/**
 * An actor that performs a transformer evaluation. It verifies and optimizes the transformer, starts all plugin instance
 * evaluations, takes care of sending the plugin instance evaluation outputs to appropriate inputs, tracks the time
 * spent evaluating, tracks the evaluation progress and responds to the control messages.
 * @param transformer The transformer to evaluate.
 * @param timeout The maximal time limit allowed for the evaluation to take in milliseconds.
 */
class TransformerEvaluation(val transformer: Transformer, private val timeout: Option[Long]) extends Actor
{
    private val timer = new Timer(timeout, this)

    private val instanceEvaluations = new mutable.ArrayBuffer[TransformerInstanceEvaluation]

    private var progress: TransformerEvaluationProgress = TransformerEvaluationProgress(Nil, Map.empty, Nil, Map.empty)

    private var result: Option[TransformerResult] = None

    def act() {
        val optimizedTransformer = optimizeTransformer()

        def startInstanceEvaluation(instance: TransformerPluginInstance, outputProcessor: Option[Graph] => Unit) {
            val evaluation = new TransformerInstanceEvaluation(instance, this, outputProcessor)
            instanceEvaluations += evaluation
            evaluation.start()

            // Start the preceding plugin evaluations.
            optimizedTransformer.pluginInstanceInputBindings(instance).foreach { binding =>
                val instanceOutputProcessor = bindingOutputProcessor(evaluation, binding.targetInputIndex) _
                startInstanceEvaluation(binding.sourcePluginInstance, instanceOutputProcessor)
            }
        }

        // Start the evaluation of the transformer by starting the output plugin instance.
        timer.start()
        progress = TransformerEvaluationProgress(Nil, Map.empty, optimizedTransformer.allOriginalInstances, Map.empty)
        startInstanceEvaluation(optimizedTransformer.outputInstance.get, transformerOutputProcessor)

        loop {
            react {
                case TransformerInstanceEvaluationProgress(i, v) => {
                    optimizedTransformer.originalInstances(i).foreach { originalInstance =>
                        progress = progress.withChangedProgress(originalInstance, v)
                    }
                }
                case TransformerInstanceEvaluationError(i, t) => {
                    optimizedTransformer.originalInstances(i).foreach { originalInstance =>
                        progress = progress.withError(originalInstance, t)
                    }
                }
                case TransformerInstanceEvaluationInput(_, graph) => {
                    finishEvaluation(graph.map(g => Success(g, progress.errors)).getOrElse {
                        Error(
                            new TransformerException("An error occured during evaluation of the transformer."),
                            progress.errors
                        )
                    })
                }
                case TIMEOUT => finishEvaluation(Timeout)
                case control: TransformerEvaluationControl => processControlMessage(control)
            }
        }
    }

    /**
     * Progress of the transformer evaluation.
     */
    def getProgress: TransformerEvaluationProgress = {
        (this !? GetProgress).asInstanceOf[TransformerEvaluationProgress]
    }

    /**
     * Result of the transformer evaluation. [[scala.None]] in case the evaluation hasn't finished yet.
     */
    def getResult: Option[TransformerResult] = {
        (this !? GetResult).asInstanceOf[Option[TransformerResult]]
    }

    /**
     * Whether the transformer evaluation has finished.
     */
    def isFinished: Boolean = {
        getResult.isDefined
    }

    /**
     * Prepares the transformer before the actual evaluation.
     * @return The prepared optimized transformer.
     */
    private def optimizeTransformer(): OptimizedTransformer = {
        try {
            transformer.checkValidity()
        } catch {
            case throwable => finishEvaluation(Error(throwable, progress.errors))
        }

        val optimizer = new TransformerOptimizer(List(
            new TransformerMergeConstructs,
            new TransformerMergeJoins,
            new TransformerMergeLimit,
            new TransformerMergeFetchersWithQueries
        ))
        optimizer.optimize(transformer)
    }

    /**
     * A function that takes a plugin instance evaluation output and sends it to the specified input.
     * @param targetEvaluation The target plugin instance evaluation.
     * @param targetInputIndex Index of the target plugin instance evaluation input.
     * @param output The output graph to send.
     */
    private def bindingOutputProcessor(targetEvaluation: TransformerInstanceEvaluation, targetInputIndex: Int)
        (output: Option[Graph]) {
        targetEvaluation ! TransformerInstanceEvaluationInput(targetInputIndex, output)
    }

    /**
     * A function that takes the output of the output plugin instance evaluation and sends it to the transformer
     * evaluation.
     * @param output The output graph to send.
     */
    private def transformerOutputProcessor(output: Option[Graph]) {
        this ! TransformerInstanceEvaluationInput(0, output)
    }

    /**
     * Processes transformer evaluation control messages.
     * @param message The control message to process.
     */
    private def processControlMessage(message: TransformerEvaluationControl) {
        message match {
            case GetProgress => reply(progress)
            case GetResult => reply(result)
            case Stop if result.isEmpty => finishEvaluation(Stopped)
            case Terminate => {
                terminateDependentActors()
                exit()
            }
        }
    }

    /**
     * Finishes the transformer evaluation and starts to respond only to control messages.
     * @param transformerResult The result to finish the transformer evaluation with.
     */
    private def finishEvaluation(transformerResult: TransformerResult) {
        terminateDependentActors()
        result = Some(transformerResult)

        // Respond only to control messages.
        loop {
            react {
                case control: TransformerEvaluationControl => processControlMessage(control)
                case _ =>
            }
        }
    }

    /**
     * Terminates all the dependent actors.
     */
    private def terminateDependentActors() {
        timer ! None
        instanceEvaluations.foreach(_ ! None)
    }
}