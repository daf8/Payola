package cz.payola.model.components

import cz.payola.data._
import cz.payola.domain.entities._
import cz.payola.domain.entities.plugins._
import cz.payola.model._
import cz.payola.domain.entities.plugins.parameters._
import cz.payola.domain.entities.plugins.parameters.StringParameterValue
import scala.collection.mutable.HashMap
import cz.payola.domain.entities.transformers.evaluation._
import cz.payola.domain.IDGenerator
import cz.payola.common._
import cz.payola.domain.entities.plugins.concrete.data._
import cz.payola.domain.entities.plugins.concrete.query._
import scala.collection.mutable
import cz.payola.common.entities.transformers.TransformerPluginInstanceBinding
import cz.payola.common.entities.transformers.TransformerCompatibilityCheck
import scala.Some
import cz.payola.data.squeryl.entities.TransformerResult
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import cz.payola.domain.DomainException
import cz.payola.common.TransformerEvaluationError
import cz.payola.domain.entities.transformers.evaluation.Success
import cz.payola.common.TransformerEvaluationInProgress
import cz.payola.domain.entities.transformers.evaluation.Error
import cz.payola.common.TransformerEvaluationSuccess
import cz.payola.domain.entities.plugins.concrete.DataFetcher
import java.net.ConnectException
import cz.payola.common.CheckError
import cz.payola.common.CheckSuccess
import cz.payola.domain.rdf._
import java.io.InputStream
import java.io.ByteArrayInputStream

trait
TransformerModelComponent extends EntityModelComponent
{
    self: DataContextComponent with PrivilegeModelComponent =>
    val runningTransformerEvaluations: HashMap[String, (Option[User], TransformerEvaluation, Long)] = new
            HashMap[String, (Option[User], TransformerEvaluation, Long)]

    lazy val transformerModel = new ShareableEntityModel(transformerRepository, classOf[Transformer])
    {
        private var checkResult = true
        private var checkDone = false
        private var checkError = ""

        def addBinding(transformerId: String, sourceId: String, targetId: String, inputIndex: Int) {
            getById(transformerId).map {
                a =>
                    val source = a.pluginInstances.find(_.id == sourceId)
                    val target = a.pluginInstances.find(_.id == targetId)

                    if (!source.isDefined || !target.isDefined) {
                        throw new Exception("Invalid source or target.")
                    }

                    a.addBinding(source.get, target.get, inputIndex)
            }.getOrElse {
                throw new Exception("Unknown transformer.")
            }
        }

        def addChecking(transformerId: String, pluginInstanceId: String, analysis: Analysis) {
            getById(transformerId).map {
                a =>
                    val pluginInstance = a.pluginInstances.find(_.id == pluginInstanceId)
                    if (!pluginInstance.isDefined) {
                        throw new Exception("Invalid plugin instance or data source.")
                    }
                    a.addChecking(pluginInstance.get, analysis)
            }.getOrElse {
                throw new Exception("Unknown transformer.")
            }
        }

        def addTransformerChecking(transformerId: String, pluginInstanceId: String, transformer: Transformer) {
            getById(transformerId).map {
                a =>
                    val pluginInstance = a.pluginInstances.find(_.id == pluginInstanceId)
                    if (!pluginInstance.isDefined) {
                        throw new Exception("Invalid plugin instance or data source.")
                    }
                    a.addTransformerChecking(pluginInstance.get, transformer)
            }.getOrElse {
                throw new Exception("Unknown transformer.")
            }
        }

        def removeChecking(transformerId: String, pluginInstanceId: String) {
            getById(transformerId).map {
                a =>
                    val pluginInstance = a.pluginInstances.find(_.id == pluginInstanceId)
                    if (!pluginInstance.isDefined) {
                        throw new Exception("Invalid plugin instance.")
                    }
                    a.compatibilityChecks.filter(_.sourcePluginInstance.id == pluginInstanceId).foreach {
                        b =>
                            a.removeChecking(b)
                    }
                    a.compatibilityTransformerChecks.filter(_.sourcePluginInstance.id == pluginInstanceId).foreach {
                        b =>
                            a.removeTransformerChecking(b)
                    }
            }.getOrElse {
                throw new Exception("Unknown transformer.")
            }
        }

        /**
         * Clones the supplied plugin instances and bind them with the target transformer.
         * @param original List of plugin instances to be cloned
         * @param bindings List of original bindings
         * @param targetTransformer Transformer to clone to
         * @return Map with the following structure (originalPluginInstanceID -> clonedPluginInstanceID)
         * @author Jiri Helmich
         */
        private def clonePluginInstances(original: Seq[TransformerPluginInstance], bindings: Seq[TransformerPluginInstanceBinding],
            targetTransformer: Transformer) : HashMap[String, String] = {
            val translateMap = HashMap[String, String]()

            original.map {
                p =>
                    val instance = createPluginInstance(p.plugin.id, targetTransformer.id)
                    translateMap.put(p.id, instance.id)

                    transformerRepository.getById(targetTransformer.id).map {
                        a =>
                            a.pluginInstances.find(_.id == instance.id).map {
                                pi =>
                                    p.parameterValues.map {
                                        v =>
                                            setParameterValue(pi, v.parameter.name, v.value.toString)
                                    }
                            }
                    }
            }

            bindings.map {
                b =>
                    addBinding(targetTransformer.id, translateMap.get(b.sourcePluginInstance.id).get,
                        translateMap.get(b.targetPluginInstance.id).get, b.targetInputIndex)
            }

            translateMap
        }

        /**
         * Clone an transformer
         * @param transformerId The transformer to be cloned
         * @param newOwner The owner of the new transformer (need to has access to the original one)
         * @return cloned transformer
         * @author Jiri Helmich
         */
        def clone(transformerId: String, newOwner: Option[User]): Transformer = {
            getAccessibleToUser(newOwner).find(_.id == transformerId).map {
                a =>
                    val newTransformer = new Transformer(a.name +"_"+ IDGenerator.newId, newOwner)
                    newTransformer.description = a.description
                    newTransformer.isVisibleInListings = a.isVisibleInListings
                    persist(newTransformer)
                    clonePluginInstances(a.pluginInstances, a.pluginInstanceBindings, newTransformer)
                    getById(newTransformer.id).get
            }.getOrElse {
                throw new ModelException("Unknown transformer ID.")
            }
        }

        def create(owner: User, name: String): Transformer = {
            val transformer = new Transformer(name, Some(owner))
            persist(transformer)
            transformer
        }

        def createPluginInstance(pluginId: String, transformerId: String): TransformerPluginInstance = {
            val transformer = transformerRepository.getById(transformerId).getOrElse {
                throw new ModelException("Unknown transformer ID.")
            }

            val instance = pluginRepository.getById(pluginId).map(_.createTransformerInstance()).getOrElse {
                throw new ModelException("Unknown plugin ID.")
            }

            transformer.addPluginInstance(instance)
            instance
        }

        def setParameterValue(user: User, transformerId: String, pluginInstanceId: String, parameterName: String,
            value: Any) {
            val transformer = user.ownedTransformers
                .find(_.id == transformerId)
                .get

            val pluginInstance = transformer.pluginInstances.find(_.id == pluginInstanceId)
            pluginInstance.map {
                i => setParameterValue(i, parameterName, value)
            }.getOrElse {
                throw new ModelException("Unknown plugin instance ID.")
            }
        }

        /**
         * @author Jiri Helmich
         */
        def setParameterValue(pluginInstance: TransformerPluginInstance, parameterName: String, value: Any) {
            if (!pluginInstance.isEditable) {
                throw new ModelException("The plugin instance is not editable.")
            }

            val option = pluginInstance.getParameterValue(parameterName)

            if (!option.isDefined) {
                throw new Exception("Unknown parameter name: " + parameterName + ".")
            }

            val parameterValue = option.get

            parameterValue match {
                case v: BooleanParameterValue => v.value = value match {
                    case x: Boolean => x
                    case s: String => s.toBoolean
                    case _ => false
                }
                case v: FloatParameterValue => v.value = value match {
                    case x: Float => x
                    case s: String => s.toFloat
                    case _ => 0.toFloat
                }
                case v: IntParameterValue => v.value = value match {
                    case x: Int => x
                    case s: String => s.toInt
                    case _ => 0
                }
                case v: StringParameterValue => v.value =  value match {
                    case s: String => s.toString
                    case _ => ""
                }
                case _ => throw new Exception("Unknown parameter type.")
            }

            transformerRepository.persistParameterValue(parameterValue)
        }

        def removePluginInstanceById(transformerId: String, pluginInstanceId: String): Boolean = {
            val transformer = transformerRepository.getById(transformerId).getOrElse {
                throw new ModelException("Unknown transformer ID.")
            }

            val instance = transformer.pluginInstances.find(_.id == pluginInstanceId).getOrElse {
                throw new ModelException("Unknown plugin instance ID.")
            }

            transformer.removePluginInstance(instance)
            transformer.pluginInstances.contains(instance)
        }

        def removePluginInstanceBindingById(transformerId: String, pluginInstanceBindingId: String): Boolean = {
            val transformer = transformerRepository.getById(transformerId).getOrElse {
                throw new ModelException("Unknown transformer ID.")
            }

            val binding = transformer.pluginInstanceBindings.find(_.id == pluginInstanceBindingId).getOrElse {
                throw new ModelException("Unknown plugin instance ID.")
            }

            transformer.removeBinding(binding)
            transformer.pluginInstanceBindings.contains(binding)
        }

        def run(transformer: Transformer, oldEvaluationId: String, user: Option[User] = None) = {
            if (runningTransformerEvaluations.isDefinedAt(oldEvaluationId)) {
                if (!runningTransformerEvaluations.get(oldEvaluationId).filter(_._2.transformer.id == transformer.id).isEmpty) {
                    runningTransformerEvaluations.remove(oldEvaluationId)
                }
            }

            // Substitute transformer plugin - inner transformers [Jiri Helmich]
            transformer.expand(getAccessibleToUser(user))

            val evaluationId = IDGenerator.newId
            runningTransformerEvaluations
                .put(evaluationId, (user, transformer.evaluate(), (new java.util.Date).getTime))

            evaluationId
        }

        def checkDS(transformer: Transformer, oldEvaluationId: String, user: Option[User] = None) = {
            if(!transformer.pluginInstances.isEmpty){
                try{
                    checkResult=transformer.pluginInstances.map { p =>
                        p.plugin match {
                            case x: DataFetcher => {
                                x.transformerAskQuery(p)
                            }
                            case _ => {
                                true
                            }
                        }
                    }.reduceLeft((a,b) => a && b)
                } catch {
                    case e: QueryParseException => checkError = "ASK query is not valid ASK SPARQL query."
                    case e: ConnectException => checkError = "Connection error during checking. Please try again later."
                    case e => throw e
                }
                checkDone=true
            } else {
                checkError = "Transformer doesn't contain any plugins."
            }
            val evaluationId = IDGenerator.newId
            evaluationId
        }

        def checkDSResult(transformer: Transformer, user: Option[User] = None): Boolean = {
            transformer.pluginInstances.map { p =>
                p.plugin match {
                    case x: DataFetcher => {
                        x.transformerAskQuery(p)
                    }
                    case _ => {
                        true
                    }
                }
            }.reduceLeft((a,b) => a && b)
        }

        def getCheckState(checkId: String, user: Option[User] = None) : CheckState = {
            if(checkError.length()>0){
                val error = checkError
                checkError = ""
                new CheckError(error)
            } else {
                if(checkDone){
                    checkDone = false
                    new CheckSuccess(checkResult)
                } else {
                    new CheckInProgress
                }
            }
        }

        private def getEvaluationTupleForID(id: String) = {
            val date = new java.sql.Timestamp(System.currentTimeMillis)
            runningTransformerEvaluations.foreach {
                tuple =>
                    if (tuple._2._3 + (20 * 60 * 1000) < date.getTime) {
                        runningTransformerEvaluations.remove(tuple._1)
                    }
            }

            runningTransformerEvaluations.get(id).getOrElse {
                throw new ModelException("The evaluation is not running.")
            }
        }

        def getEvaluationState(evaluationId: String, user: Option[User] = None) : TransformerEvaluationState = {
            val evaluationTuple = getEvaluationTupleForIDAndPerformSecurityChecks(evaluationId, user)

            runningTransformerEvaluations.put(evaluationId, (evaluationTuple._1, evaluationTuple._2, System.currentTimeMillis))

            val evaluation = evaluationTuple._2

            evaluation.getResult.map {
                case r: Error => TransformerEvaluationError(transformException(r.error),
                    r.instanceErrors.toList.map {
                        e => (e._1, transformException(e._2))
                    })
                case r: Success =>
                    TransformerEvaluationSuccess(r.outputGraph,
                        r.instanceErrors.toList.map {
                            e => (e._1, transformException(e._2))
                        })
                case Timeout => new TransformerEvaluationTimeout
                case _ => throw new Exception("Unhandled evaluation state")
            }.getOrElse {
                val progress = evaluation.getProgress
                TransformerEvaluationInProgress(progress.value, progress.evaluatedInstances, progress.runningInstances.toList,
                    progress.errors.toList.map {
                        e => (e._1, transformException(e._2))
                    })
            }
        }

        private def transformException(t: Throwable): String = {
            t match {
                case e: Exception => e.getMessage
                case _ => "Unknown error."
            }
        }

        def getEvaluationTupleForIDAndPerformSecurityChecks(id: String, user: Option[User]) = {
            val evaluationTuple = getEvaluationTupleForID(id)
            if (!evaluationTuple._1.isDefined || evaluationTuple._1 == user) {
                evaluationTuple
            } else {
                throw new ModelException("Forbidden evaluation.")
            }
        }

        /**
         * Crucial part of the LODVis integration - creating an anonymous transformer. The persistance needs to be split
         * into several steps due to the current state of the DAL.
         *
         * Based on endpoint URI, list of graph URIs and class and/or property URI, an anonymous transformer is created.
         * Also a token is added in order to make the user able to take the ownership, when logged in.
         *
         * @return transformer
         * @author Jiri Helmich
         */
        def createAnonymousTransformer(user: Option[User], endpointUri: String, graphUris: List[String],
            classUri: Option[String], propertyUri: Option[String]) = {
            lazy val endpointPluginId = pluginRepository.getByName("SPARQL Endpoint").map(_.id).getOrElse("")
            lazy val typedPluginId = pluginRepository.getByName("Typed").map(_.id).getOrElse("")
            lazy val propertyPluginId = pluginRepository.getByName("Property Selection").map(_.id).getOrElse("")

            val transformer = new Transformer(IDGenerator.newId, user)
            transformer.isPublic = true
            transformer.token = Some(IDGenerator.newId)
            transformer.isVisibleInListings = false
            persist(transformer)

            val endpointInstance = createPluginInstance(endpointPluginId, transformer.id)

            val typedInstance = classUri.map {
                u =>
                    val typedInstance = createPluginInstance(typedPluginId, transformer.id)
                    addBinding(transformer.id, endpointInstance.id, typedInstance.id, 0)
                    typedInstance
            }

            val propertyInstance = propertyUri.map {
                u =>
                    val propertyInstance = createPluginInstance(propertyPluginId, transformer.id)
                    val instanceId = typedInstance.getOrElse(endpointInstance).id
                    addBinding(transformer.id, instanceId, propertyInstance.id, 0)
                    propertyInstance
            }

            val persistedTransformer = transformerRepository.getById(transformer.id)
            persistedTransformer.map {
                a =>

                    val persistedInstances = a.pluginInstances

                    persistedInstances.find(_.id == endpointInstance.id).map {
                        e =>
                            setParameterValue(e, SparqlEndpointFetcher.endpointURLParameter, endpointUri)
                            setParameterValue(e, SparqlEndpointFetcher.graphURIsParameter, graphUris.mkString("\n"))
                    }

                    typedInstance.map {
                        t =>
                            persistedInstances.find(_.id == t.id)
                                .map(setParameterValue(_, Typed.typeURIParameter, classUri.get))
                    }
                    propertyInstance.map {
                        p =>
                            persistedInstances.find(_.id == p.id)
                                .map(setParameterValue(_, PropertySelection.propertyURIsParameter, propertyUri.get))
                    }
            }

            transformer
        }

        /**
         * Changes the ownership of the transformer based on security token from cookie.
         * @param transformerId ID of the transformer to change owner of.
         * @param user The new owner.
         * @param availableTokens List of available tokens.
         * @author Jiri Helmich
         */
        def takeOwnership(transformerId: String, user: User, availableTokens: Seq[String]) {
            getById(transformerId).map {
                a =>
                    val canTakeOwnership = a.token.isDefined && availableTokens.contains(a.token.get)

                    if (canTakeOwnership) {
                        a.owner = Some(user)
                        a.token = None
                        persist(a)
                    }
            }
        }

        /**
         * Make partial transformer. The plugin instance parameter tells us, which plugin instance in the transformer is the
         * limiting one for the subtransformer. We extract those plugins that are used to prepare data for the supplied
         * instance (topological order, precceeding).
         *
         * The extracted sub-pipeline is appended with an instance of a limit plugin.
         *
         * @param transformer The transformer to make partial from.
         * @param pluginInstanceId Plugin instance to extract transformer to.
         * @param limitCount Size of the limit
         * @return Partial transformer
         * @author Jiri Helmich
         */
        def makePartial(transformer: Transformer, pluginInstanceId: String, limitCount: Int = 20): Option[String] = {
            val lastOutput = transformer.pluginInstanceBindings.find(_.targetPluginInstance.id == pluginInstanceId)

            lastOutput.map {
                o =>
                    val waiting = mutable.Queue(o.sourcePluginInstance)
                    val instances = new mutable.ListBuffer[TransformerPluginInstance]
                    val bindings = new mutable.ListBuffer[TransformerPluginInstanceBinding]

                    while (!waiting.isEmpty) {
                        val current = waiting.dequeue()
                        instances += current

                        val pre = transformer.pluginInstanceBindings.filter(_.targetPluginInstance == current)
                        pre.map {
                            b =>
                                waiting.enqueue(b.sourcePluginInstance)
                                bindings += b
                        }
                    }

                    val partial = new Transformer(pluginInstanceId, transformer.owner)
                    partial.isPublic = false
                    partial.token = None
                    try {
                        persist(partial)
                    } catch {
                        case e: ValidationException =>
                            val transformer = transformerRepository.getByName(pluginInstanceId)
                            transformer.map {
                                a =>
                                    transformerRepository.removeById(a.id)
                                    persist(partial)
                            }
                    }

                    val translateMap = clonePluginInstances(instances, bindings, partial)

                    //to make a sensible limit, we need to append sparql construct query with a limit param
                    //CONSTRUCT { ?x ?y ?z } WHERE { ?x ?y ?z  } LIMIT 2

                    lazy val limitPluginId = pluginRepository.getByName("Limit").map(_.id).getOrElse("")
                    val limitInstance = createPluginInstance(limitPluginId, partial.id)

                    val persistedTransformer = transformerRepository.getById(partial.id)
                    persistedTransformer.map {
                        a =>

                            val persistedInstances = a.pluginInstances

                            persistedInstances.find(_.id == limitInstance.id).map {
                                e =>
                                    setParameterValue(e, Limit.limitCountParameter, limitCount)
                                    addBinding(partial.id, translateMap.get(o.sourcePluginInstance.id).get, e.id, 0)
                            }
                    }


                    Some(partial.id)
            }.getOrElse(None)
        }

        def executeQuery(ttl: String, query: String): Boolean = {
            val model: Model = ModelFactory.createDefaultModel()
            val is: InputStream = new ByteArrayInputStream(ttl.getBytes("UTF-8"))
            model.read(is,null,"TTL")
            val graph = new JenaGraph(model)
            graph.executeSPARQLAskQuery(query)
        }
    }
}