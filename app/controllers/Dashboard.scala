package controllers

import play.api._, mvc._
import utils.Secure

object Dashboard extends Controller with Secure {

  def index =  SecuredAction {
    Ok(views.html.dashboard("Your new application is ready."))
  }
}
