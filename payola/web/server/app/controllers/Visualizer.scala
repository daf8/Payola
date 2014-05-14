package controllers

import helpers.Secured
import cz.payola.domain.entities._
import cz.payola.web.shared._
import play.mvc.Security.Authenticated
import cz.payola.model.ModelException

object Visualizer extends PayolaController with Secured
{
    def check = authenticated { user =>
        Ok(views.html.visualizer(user))
    }
}
