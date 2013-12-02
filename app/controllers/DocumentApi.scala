package controllers

import scala.concurrent._, ExecutionContext.Implicits.global
import play.api._, mvc._, libs.json._
import play.api.data._, Forms._, validation.Constraints._
import models._

object DocumentApi extends Controller with Secure {

  val documentForm = Form(
    single(
      "title" -> nonEmptyText
    )
  )

  def list = SecuredAction.async { implicit request =>
    Document.getByOwnerId(request.user.id).map { documents =>
      Ok(
        Json.obj(
          "count" -> documents.length,
          "items" -> Json.arr(documents)
        )
      )
    }
  }

  def create = SecuredAction.async(parse.json) { implicit request =>
    documentForm.bind(request.body).fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      title => Document.create(title, request.user) map { document =>
        Created(Json.toJson(document))
      }
    )
  }
}
