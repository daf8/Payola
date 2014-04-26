package cz.payola.domain.entities.transformers.optimization.phases

import cz.payola.domain.entities.transformers.optimization.plugins.ConstructJoinPluginInstance
import cz.payola.domain.entities.plugins.TransformerPluginInstance
import cz.payola.domain.entities.plugins.concrete._
import cz.payola.domain.entities.plugins.concrete.query.Construct
import cz.payola.domain.entities.transformers.optimization._

/**
 * Merges joins of two construct plugin instances that receive data from the same data fetcher.
 */
class TransformerMergeJoins extends TransformerOptimizationPhase
{
    def run(transformer: OptimizedTransformer): OptimizedTransformer = {
        merge(transformer, transformer.outputInstance.get)
        transformer
    }

    /**
     * Merge the specified plugin instance with preceding instance in case it's possible. Merges all the preceding
     * instances with their predecessors recursively.
     * @param transformer The transformer where the merge is performed.
     * @param instance The instance to merge.
     */
    def merge(transformer: OptimizedTransformer, instance: TransformerPluginInstance) {
        transformer.pluginInstanceInputBindings(instance).foreach(b => merge(transformer, b.sourcePluginInstance))
        instance.plugin match {
            case join: Join => {
                val instanceInputBindings = transformer.pluginInstanceInputBindings
                val outputBindings = transformer.pluginInstanceOutputBindings(instance)

                // Matches chains consisting af a DataFetcher and a Construct above the Join. For example:
                //
                //     DataFetcher1 --> Construct1 --> |------|
                //                                     | Join | --> Whatever
                //     DataFetcher2 --> Construct2 --> |------|
                //
                // will result in: List((Construct1, DataFetcher1), (Construct2, DataFetcher2))
                val chains = instanceInputBindings(instance).sortBy(_.targetInputIndex).flatMap { binding =>
                    val source = binding.sourcePluginInstance
                    val sourceOfSource = instanceInputBindings(source).headOption.map(_.sourcePluginInstance)
                    sourceOfSource.map(_.plugin).flatMap {
                        case _: DataFetcher if source.plugin.isInstanceOf[Construct] => {
                            source.plugin match {
                                case c: Construct => Some(TransformerPluginWithInstance(c, source), sourceOfSource.get)
                                case _ => None
                            }
                        }
                        case _ => None
                    }
                }

                // If there are two chains with same data source, merges the constructs and the join into one plugin
                // instance. The above example will result in:
                //
                //     DataFetcher1 --> ConstructJoin --> Whatever
                //
                chains.toList match {
                    case List((subjectConstruct, subjectFetcher), (objectConstruct, objectFetcher)) => {
                        val dataFetcherParametersAreEqual = subjectFetcher.parameterValues.forall { value =>
                            objectFetcher.getParameter(value.parameter.name).map(_ == value.value).getOrElse(false)
                        }
                        if (subjectFetcher.plugin == objectFetcher.plugin && dataFetcherParametersAreEqual) {
                            // Merge the instances.
                            val joinInstance = new ConstructJoinPluginInstance(TransformerPluginWithInstance(join, instance),
                                subjectConstruct, objectConstruct)
                            transformer.replaceInstances(joinInstance, objectFetcher, subjectConstruct.instance,
                                objectConstruct.instance, instance)

                            // Restore the bindings.
                            transformer.addBinding(subjectFetcher, joinInstance)
                            outputBindings.foreach { b =>
                                transformer.addBinding(joinInstance, b.targetPluginInstance, b.targetInputIndex)
                            }
                        }
                    }
                    case _ =>
                }
            }
            case _ =>
        }
    }
}
