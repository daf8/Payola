package cz.payola.web.shared

import s2js.compiler._
import cz.payola.model.ModelException
import cz.payola.domain.entities._
import cz.payola.web.shared.managers._
import cz.payola.common._
import cz.payola.domain.rdf.Graph
import cz.payola.common.TransformerEvaluationSuccess

@remote
@secured object  TransformerRunner
    extends ShareableEntityManager[Transformer, cz.payola.common.entities.Transformer](Payola.model.transformerModel)
{
    @async def runTransformerById(id: String, oldEvaluationId: String, user: Option[User] = None)
        (successCallback: (String => Unit))
        (failCallback: (Throwable => Unit)) {

        val transformer = getTransformerById(user, id)
        val evaluationId = Payola.model.transformerModel.run(transformer, oldEvaluationId, user)

        successCallback(evaluationId)
    }

    @async def getEvaluationState(evaluationId: String, transformerId: String, user: Option[User] = None)
        (successCallback: (TransformerEvaluationState => Unit))
        (failCallback: (Throwable => Unit)) {

        val resultResponse =
            try{
                val response = Payola.model.transformerModel.getEvaluationState(evaluationId, user)
                response match {
                    case r: TransformerEvaluationSuccess =>
                        Payola.model.transformerResultStorageModel.saveGraph(
                            r.outputGraph, transformerId, evaluationId)
                        val availableTransformators: List[String] =
                            TransformationManager.getAvailableTransformations(r.outputGraph)
                        TransformerEvaluationCompleted(availableTransformators, r.instanceErrors)

                    case _ =>
                        response
                }
            } catch {
                case e: ModelException => // the evaluation was never started, the result is in resultStorage
                    val graph = Payola.model.transformerResultStorageModel.getGraph(evaluationId)
                    val availableTransformators: List[String] = TransformationManager.getAvailableTransformations(graph)
                    TransformerEvaluationCompleted(availableTransformators, List())
                case p =>  {
                    throw p
                }
            }

        successCallback(resultResponse)
    }

    @async def evaluationExists(evaluationId: String, user: Option[User] = None)(successCallback: (Boolean => Unit))
        (failCallback: (Throwable => Unit)) {

        successCallback(Payola.model.transformerResultStorageModel.exists(evaluationId))
    }

    @async def runCheck(id: String, oldCheckId: String,
        checkTransformerStore: Boolean = false, user: Option[User] = None)
        (successCallback: (String => Unit))
        (failCallback: (Throwable => Unit)) {
        val transformer = getTransformerById(user, id)
        val checkId = Payola.model.transformerModel.checkDS(transformer, oldCheckId, user)
        successCallback(checkId)
    }

    @async def getCheckState(checkId: String, transformerId: String, user: Option[User] = None)
        (successCallback: (CheckState => Unit))
        (failCallback: (Throwable => Unit)) {
        val resultResponse: CheckState = Payola.model.transformerModel.getCheckState(checkId, user)
        successCallback(resultResponse)
    }

    /**
     * Partial transformer remote proxy
     * @param transformerId Transformer to make partial from
     * @param pluginInstanceId Plugin instance which makes the cutting point of the transformer
     * @param limitCount Limit plugin parameter value
     * @param user owner
     * @param successCallback
     * @param failCallback
     * @return
     * @author Jiri Helmich
     */
    @async def createPartialTransformer(transformerId: String, pluginInstanceId: String, limitCount: Int, user: Option[User] = None)
        (successCallback: (String => Unit))
        (failCallback: (Throwable => Unit)) {
        val transformer = getTransformerById(user, transformerId)
        val partialTransformerId = Payola.model.transformerModel.makePartial(transformer, pluginInstanceId, limitCount)

        if (partialTransformerId.isDefined){
            successCallback(partialTransformerId.get)
        }
    }

    private def getTransformerById(user: Option[User], id: String): Transformer = {
        Payola.model.transformerModel.getAccessibleToUserById(user, id).getOrElse {
            throw new ModelException("The transformer doesn't exist.")
        }
    }
}