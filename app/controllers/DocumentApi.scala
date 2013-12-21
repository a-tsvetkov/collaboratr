package controllers

import scala.concurrent._, ExecutionContext.Implicits.global
import play.api._, mvc._, libs.iteratee._, libs.json._
import play.api.data._, Forms._, validation.Constraints._
import utils.Secure
import models._

object DocumentApi extends Controller with Secure {

  val logger = Logger("application")

  val documentForm = Form(
    single(
      "title" -> nonEmptyText))

  def list = SecuredAction.async { implicit request =>
    Document.getByOwnerId(request.user.id).map { documents =>
      Ok(
        Json.obj(
          "count" -> documents.length,
          "items" -> documents))
    }
  }

  def item(id: Long) = SecuredAction.async { implicit request =>
    Document.getById(id) map {
      _.map { document =>
        Ok(Json.toJson(document))
      }.getOrElse(NotFound("No document with id " + id))
    }
  }

  def create = SecuredAction.async(parse.json) { implicit request =>
    documentForm.bind(request.body).fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      title => Document.create(title, request.user) map { document =>
        Created(Json.toJson(document))
      })
  }

  def delete(id: Long) = SecuredAction.async { implicit request =>
    Document.remove(id, request.user.id) map {
      _ match {
        case 1 => NoContent
        case 0 => Forbidden("Access Denied")
      }
    }
  }

  def update = WebSocket.async[JsValue] { implicit request =>
    getUser(request).map {
      _.map {
        user =>
          val in = Iteratee.foreach[JsValue] { message =>
            logger.debug(message.toString)
          }
          val out = Enumerator[JsValue](Json.obj("message" -> "test"))

          (in, out)
      }.getOrElse {
        (Iteratee.ignore, Enumerator[JsValue](Json.obj("message" -> "Forbidden")).andThen(Enumerator.eof))
      }
    }
  }
}
