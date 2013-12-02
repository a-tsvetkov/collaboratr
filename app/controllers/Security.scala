package controllers

import play.api._, mvc._
import play.api.data._, Forms._, validation.Constraints._
import scala.concurrent._, ExecutionContext.Implicits.global
import models.User

case class UserData(email: String, password: String)
case class UserRegistrationData(email: String, password: String, p2: String)

object Security extends Controller {

  val loginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(UserData.apply)(UserData.unapply)
  )

  def validateRegistrationForm(email: String, password: String, p2: String) = {
    if (password == p2) {
      Some(UserRegistrationData(email, password, p2))
    } else {
      None
    }
  }

  val registrationForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(8, 100),
      "p2" -> nonEmptyText(8, 100)
    )(UserRegistrationData.apply)(UserRegistrationData.unapply) verifying (
      "Password must match!",
      _ match {
        case data: UserRegistrationData => validateRegistrationForm(data.email, data.password, data.p2).isDefined
      }
    )
  )

  def loginUser(user: User)(implicit request: Request[AnyContent]) = {
    Redirect(routes.Dashboard.index).withSession(session + ("user_id" -> user.id.toString))
  }

  def login = Action {
    Ok(views.html.login(loginForm))
  }

  def checkLogin = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      userData => User.authenticate(userData.email, userData.password) map {
        _ match {
          case Some(user) => loginUser(user)
          case None => Unauthorized(views.html.login(loginForm.bind(Map("email" -> userData.email))))
        }
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.Dashboard.index()).withSession(session - "user_id")
  }

  def signup = Action {
    Ok(views.html.register(registrationForm))
  }

  def checkSignup = Action.async { implicit request =>
    val boundForm = registrationForm.bindFromRequest
    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(views.html.register(formWithErrors))),
      registrationData => User.getByEmail(registrationData.email) flatMap {
        _ match {
          case Some(u) => Future.successful(Conflict(views.html.register(boundForm)))
          case None => User.create(registrationData.email, registrationData.password) map {
            loginUser(_)
          }
        }
      }
    )
  }

}
