package cz.payola.web.shared

import cz.payola.domain.entities.User
import s2js.compiler._
import cz.payola.common.entities._

@remote @secured object DomainData
{
    @async def searchMembersAvailableForGroup(groupId: String, term: String, owner: User = null)(successCallback: (Seq[User] => Unit))
        (failCallback: (Throwable => Unit)) {
        val group = Payola.model.groupModel.getById(groupId).getOrElse{
            throw new Exception("Group not found.")
        }

        val users = Payola.model.groupModel.findAvailableMembers(group, owner, term)
        successCallback(users)
    }

    @async def searchAccessibleAnalyses(term: String, user: Option[User] = null)(successCallback: (Seq[Analysis] => Unit))
        (failCallback: (Throwable => Unit)) {
        val analyses = Payola.model.analysisModel.getAccessibleToUser(user).filter(_.name.contains(term))
        successCallback(analyses)
    }

    @async def searchAccessibleTransformers(term: String, user: Option[User] = null)(successCallback: (Seq[Transformer] => Unit))
        (failCallback: (Throwable => Unit)) {
        val transformers = Payola.model.transformerModel.getAccessibleToUser(user).filter(_.name.contains(term))
        successCallback(transformers)
    }

    @async def getAnalysisById(analysisId: String, user: Option[User] = None)(successCallback: (Analysis => Unit))
        (failCallback: (Throwable => Unit)) {

        val analysis = Payola.model.analysisModel.getAccessibleToUserById(user,analysisId).getOrElse{
            throw new Exception("Analysis not found.")
        }

        successCallback(analysis)
    }

    @async def getTransformerById(transformerId: String, user: Option[User] = None)(successCallback: (Transformer => Unit))
        (failCallback: (Throwable => Unit)) {

        val transformer = Payola.model.transformerModel.getAccessibleToUserById(user,transformerId).getOrElse{
            throw new Exception("Transformer not found.")
        }

        successCallback(transformer)
    }

    /**
     * Clone analysis button functionality
     * @param analysisId Analysis to clone
     * @param user current user
     * @param successCallback
     * @param failCallback
     * @return
     */
    @async def cloneAnalysis(analysisId: String, user: Option[User] = None)(successCallback: (Analysis => Unit))
        (failCallback: (Throwable => Unit)) {
        successCallback(Payola.model.analysisModel.clone(analysisId, user))
    }

    /**
     * Clone transformer button functionality
     * @param transformerId Analysis to clone
     * @param user current user
     * @param successCallback
     * @param failCallback
     * @return
     */
    @async def cloneTransformer(transformerId: String, user: Option[User] = None)(successCallback: (Transformer => Unit))
        (failCallback: (Throwable => Unit)) {
        successCallback(Payola.model.transformerModel.clone(transformerId, user))
    }
}
