package controllers

import play.api._, mvc._

object Dashboard extends Controller with Secure {

  def index =  SecuredAction {
    Ok(views.html.dashboard("Your new application is ready."))
  }
}
