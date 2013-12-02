package controllers

import play.api._
import play.api.mvc._

object JsRoutes extends Controller {

  /** The javascript router. */
  def router = Action { implicit req =>
    Ok(
      Routes.javascriptRouter("routes")(
        routes.javascript.Assets.at,
        routes.javascript.Security.login,
        routes.javascript.DocumentApi.list
      )
    ).as("text/javascript")
  }
}
