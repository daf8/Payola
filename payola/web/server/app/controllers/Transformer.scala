package controllers

import helpers.Secured
import cz.payola.domain.entities._
import cz.payola.web.shared._
import play.mvc.Security.Authenticated
import cz.payola.model.ModelException

object Transformer extends PayolaController with Secured
{
    def detail(id: String) = maybeAuthenticatedWithRequest { (user, request) =>
        Payola.model.transformerModel.getById(id).map { a =>
            val canTakeOwnership = user.isDefined && a.token.isDefined && request.session.get("transformer-tokens")
                .map { tokens: String =>
                tokens.split(",").contains(a.token.get)
            }.getOrElse(false)

            Ok(views.html.transformer.detail(user, a, canTakeOwnership))
        }.getOrElse {
            NotFound(views.html.errors.err404("The transformer does not exist."))
        }
    }

    def create = authenticated { user =>
        Ok(views.html.transformer.create(user))
    }

    def edit(id: String) = authenticated { user =>
        Ok(views.html.transformer.edit(user, id))
    }

    def cloneAndEdit(id: String) = authenticated { user =>
        val transformer = Payola.model.transformerModel.clone(id, Some(user))
        Redirect(routes.Transformer.edit(transformer.id))
    }

    def delete(id: String) = authenticatedWithRequest { (user, request) =>
        user.ownedTransformers.find(_.id == id).map(Payola.model.transformerModel.remove(_))
            .getOrElse(NotFound("Transformer not found."))

        Redirect(routes.Transformer.list())
    }

    def list(page: Int = 1) = authenticated { user: User =>
        Ok(views.html.transformer.list(Some(user), user.ownedTransformers, page))
    }

    def listExecutable(page: Int = 1) = authenticated { user: User =>
        Ok(views.html.transformer.list(Some(user), user.ownedTransformers.filter(_.checked), page, Some("Executable transformers")))
    }

    def check() = authenticated { user =>
        user.ownedTransformers.map { p =>
            val checkResult = Payola.model.transformerModel.checkDSResult(getTransformerById(Some(user), p.id), Some(user))
            val transformerM = Payola.model.transformerModel.getById(p.id)
            transformerM.map {
                a =>
                    a.checked = checkResult
                    a.lastCheck = System.currentTimeMillis()
                    Payola.model.transformerModel.persist(a)
            }
        }
        Redirect(routes.Transformer.list())
    }

    private def getTransformerById(user: Option[User], id: String): Transformer = {
        Payola.model.transformerModel.getAccessibleToUserById(user, id).getOrElse {
            throw new ModelException("The transformer doesn't exist.")
        }
    }

    def listAccessible(page: Int = 1) = maybeAuthenticated { user: Option[User] =>
        Ok(views.html.transformer
            .list(user, Payola.model.transformerModel.getAccessibleToUser(user, forListing = true), page, Some("Accessible transformers")))
    }

    def listAccessibleByOwner(ownerId: String, page: Int = 1) = maybeAuthenticated { user: Option[User] =>
        val owner = Payola.model.userModel.getById(ownerId)
        val transformers = if (owner.isDefined) {
            Payola.model.transformerModel.getAccessibleToUserByOwner(user, owner.get)
        } else {
            List()
        }
        Ok(views.html.transformer.list(user, transformers, page))
    }

    def visualizeAnonymously(endpointUri: String, graphUri: List[String], classUri: Option[String],
        propertyUri: Option[String]) = maybeAuthenticatedWithRequest { (user, request) =>

        val decodeBase64 = { uri: String => new String(new sun.misc.BASE64Decoder().decodeBuffer(uri))}

        val decodedEndpointUri = decodeBase64(endpointUri)
        val decodedGraphUris = graphUri.map(decodeBase64(_))
        val decodedClassUri = classUri.map(decodeBase64(_))
        val decodedPropertyUri = propertyUri.map(decodeBase64(_))

        val transformer = Payola.model.transformerModel.createAnonymousTransformer(user, decodedEndpointUri, decodedGraphUris,
            decodedClassUri, decodedPropertyUri)

        transformer.token.map { token =>
            val currentTokens = request.session.get("transformer-tokens").map(_ + "," + token).getOrElse(token)
            Redirect(routes.Transformer.detail(transformer.id)).withSession("transformer-tokens" -> currentTokens)
        }.getOrElse {
            Redirect(routes.Transformer.detail(transformer.id))
        }
    }

    def takeOwnership(id: String) = authenticatedWithRequest { (user, request) =>
        request.session.get("transformer-tokens").map{tokens: String =>
            tokens.split(",")
        }.map{ tokens =>
            Payola.model.transformerModel.takeOwnership(id, user, tokens)
        }

        Redirect(routes.Transformer.detail(id))
    }
}
