package controllers

import play.api._
import play.api.mvc._
import utils.Secure

object JsRoutes extends Controller {

  /** The javascript router. */
  def router = Action { implicit req =>
    Ok(
      Routes.javascriptRouter("routes")(
        routes.javascript.Assets.at,
        routes.javascript.Security.login,
        routes.javascript.DocumentApi.list,
        routes.javascript.DocumentApi.item,
        routes.javascript.DocumentApi.delete
      )
    ).as("text/javascript")
  }
}
