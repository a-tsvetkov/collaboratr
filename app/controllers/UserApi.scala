package controllers

import scala.concurrent._, ExecutionContext.Implicits.global
import play.api._, mvc._, libs.json._
import play.api.data._, Forms._, validation.Constraints._
import utils.Secure

import models._

private[controllers] case class UserInfoData(firstName: String, lastName: String)

object UserApi extends Controller with Secure {

  val userInfoForm = Form(
    mapping(
      "firstName" -> text,
      "lastName" -> text)(UserInfoData.apply)(UserInfoData.unapply))

  def me = SecuredAction { implicit request =>
    Ok(Json.toJson(request.user))
  }

  def updateInfo = SecuredAction.async(parse.json) { implicit request =>
    userInfoForm.bind(request.body).fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      userInfo => User.updateInfo(request.user, userInfo.firstName, userInfo.lastName).map { user =>
        Ok(Json.toJson(user))
      })
  }
}
