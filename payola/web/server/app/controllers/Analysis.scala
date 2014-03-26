package controllers

import helpers.Secured
import cz.payola.domain.entities._
import cz.payola.web.shared._
import play.mvc.Security.Authenticated
import cz.payola.model.ModelException

object Analysis extends PayolaController with Secured
{
    def detail(id: String) = maybeAuthenticatedWithRequest { (user, request) =>
        Payola.model.analysisModel.getById(id).map { a =>
            val canTakeOwnership = user.isDefined && a.token.isDefined && request.session.get("analysis-tokens")
                .map { tokens: String =>
                tokens.split(",").contains(a.token.get)
            }.getOrElse(false)

            Ok(views.html.analysis.detail(user, a, canTakeOwnership))
        }.getOrElse {
            NotFound(views.html.errors.err404("The analysis does not exist."))
        }
    }

    def create = authenticated { user =>
        Ok(views.html.analysis.create(user))
    }

    def edit(id: String) = authenticated { user =>
        Ok(views.html.analysis.edit(user, id))
    }

    def cloneAndEdit(id: String) = authenticated { user =>
        val analysis = Payola.model.analysisModel.clone(id, Some(user))
        Redirect(routes.Analysis.edit(analysis.id))
    }

    def delete(id: String) = authenticatedWithRequest { (user, request) =>
        user.ownedAnalyses.find(_.id == id).map(Payola.model.analysisModel.remove(_))
            .getOrElse(NotFound("Analysis not found."))

        Redirect(routes.Analysis.list())
    }

    def list(page: Int = 1) = authenticated { user: User =>
        Ok(views.html.analysis.list(Some(user), user.ownedAnalyses, page))
    }

    def listExecutable(page: Int = 1) = authenticated { user: User =>
        Ok(views.html.analysis.list(Some(user), user.ownedAnalyses.filter(_.checked), page, Some("Executable analyses")))
    }

    def check() = authenticated { user =>
        user.ownedAnalyses.map { p =>
            //println(p.id)
            val checkResult = Payola.model.analysisModel.checkDSResult(getAnalysisById(Some(user), p.id), Some(user))
            val analysisM = Payola.model.analysisModel.getById(p.id)
            analysisM.map {
                a =>
                    a.checked = checkResult
                    a.lastCheck = System.currentTimeMillis()
                    Payola.model.analysisModel.persist(a)
            }
        }
        Redirect(routes.Analysis.list())
    }

    private def getAnalysisById(user: Option[User], id: String): Analysis = {
        Payola.model.analysisModel.getAccessibleToUserById(user, id).getOrElse {
            throw new ModelException("The analysis doesn't exist.")
        }
    }

    def listAccessible(page: Int = 1) = maybeAuthenticated { user: Option[User] =>
        Ok(views.html.analysis
            .list(user, Payola.model.analysisModel.getAccessibleToUser(user, forListing = true), page, Some("Accessible analyses")))
    }

    def listAccessibleByOwner(ownerId: String, page: Int = 1) = maybeAuthenticated { user: Option[User] =>
        val owner = Payola.model.userModel.getById(ownerId)
        val analyses = if (owner.isDefined) {
            Payola.model.analysisModel.getAccessibleToUserByOwner(user, owner.get)
        } else {
            List()
        }
        Ok(views.html.analysis.list(user, analyses, page))
    }

    def visualizeAnonymously(endpointUri: String, graphUri: List[String], classUri: Option[String],
        propertyUri: Option[String]) = maybeAuthenticatedWithRequest { (user, request) =>

        val decodeBase64 = { uri: String => new String(new sun.misc.BASE64Decoder().decodeBuffer(uri))}

        val decodedEndpointUri = decodeBase64(endpointUri)
        val decodedGraphUris = graphUri.map(decodeBase64(_))
        val decodedClassUri = classUri.map(decodeBase64(_))
        val decodedPropertyUri = propertyUri.map(decodeBase64(_))

        val analysis = Payola.model.analysisModel.createAnonymousAnalysis(user, decodedEndpointUri, decodedGraphUris,
            decodedClassUri, decodedPropertyUri)

        analysis.token.map { token =>
            val currentTokens = request.session.get("analysis-tokens").map(_ + "," + token).getOrElse(token)
            Redirect(routes.Analysis.detail(analysis.id)).withSession("analysis-tokens" -> currentTokens)
        }.getOrElse {
            Redirect(routes.Analysis.detail(analysis.id))
        }
    }

    def takeOwnership(id: String) = authenticatedWithRequest { (user, request) =>
        request.session.get("analysis-tokens").map{tokens: String =>
            tokens.split(",")
        }.map{ tokens =>
            Payola.model.analysisModel.takeOwnership(id, user, tokens)
        }

        Redirect(routes.Analysis.detail(id))
    }
}
