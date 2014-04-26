package cz.payola.domain.entities.transformers.optimization.plugins

import cz.payola.domain.entities.plugins._
import cz.payola.domain.entities.plugins.concrete.query.Construct
import cz.payola.domain.sparql._

/**
  * A plugin that during optimization replaces join of two construct plugins that take data from the same data fetcher.
  */
object ConstructJoinPlugin extends Construct("Joined SPARQL construct queries")
{
    def getConstructQuery(instance: PluginInstance, subject: Subject, variableGetter: () => Variable) = {
        instance match {
            case _ => throw new PluginException("This should not be called.")
        }
    }

    def transformerGetConstructQuery(instance: TransformerPluginInstance, subject: Subject, variableGetter: () => Variable) = {
        instance match {
            case constructJoinInstance: ConstructJoinPluginInstance => {
                val joinPlugin = constructJoinInstance.join.plugin
                val joinInstance = constructJoinInstance.join.instance
                val joinPropertyURI = joinPlugin.transformerGetJoinPropertyURI(joinInstance)
                val joinIsInner = joinPlugin.transformerGetIsInner(joinInstance)

                usingDefined(joinPropertyURI, joinIsInner) { (propertyURI, isInner) =>
                    val joinObjectVariable = variableGetter()

                    val subjectPlugin = constructJoinInstance.subjectConstruct.plugin
                    val subjectInstance = constructJoinInstance.subjectConstruct.instance
                    val subjectQuery = subjectPlugin.transformerGetConstructQuery(subjectInstance, subject, variableGetter)

                    val objectPlugin = constructJoinInstance.objectConstruct.plugin
                    val objectInstance = constructJoinInstance.objectConstruct.instance
                    val objectQuery = objectPlugin.transformerGetConstructQuery(objectInstance, joinObjectVariable, variableGetter)

                    val joinTriple = TriplePattern(subject, Uri(propertyURI), joinObjectVariable)
                    val joinPattern = GraphPattern(joinTriple)
                    val template = joinTriple +: (subjectQuery.template ++ objectQuery.template)
                    val subjectPattern = subjectQuery.pattern
                    val objectPattern = objectQuery.pattern

                    val pattern = if (isInner) {
                        joinPattern + subjectPattern + objectPattern
                    } else {
                        GraphPattern(Nil, List(joinPattern + objectPattern), Nil) + subjectPattern
                    }

                    ConstructQuery(template, Some(pattern))
                }
            }
            case _ => throw new PluginException("The specified plugin instance doesn't correspond to the plugin.")
        }
    }
}
