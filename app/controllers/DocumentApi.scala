package controllers

import scala.language.postfixOps
import scala.concurrent._, ExecutionContext.Implicits.global, duration._
import play.api._, mvc._, libs.iteratee._, libs.json._
import play.api.data._, Forms._, validation.Constraints._
import akka.pattern.ask
import akka.util.Timeout
import utils.Secure
import models._
import actors._

object DocumentApi extends Controller with Secure {

  implicit val timeout = Timeout(1 second)

  val logger = Logger("application")

  val updateSerializer: Enumeratee[DocumentUpdate, JsValue] =
    Enumeratee.map[DocumentUpdate] { upd => Json.toJson(upd) }

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

  def update(id: Long) = WebSocket.async[JsValue] { implicit request =>
    getUser(request).flatMap {
      _.map { user =>
        Document.getById(id).flatMap {
          _.map { document =>
            DocumentWatcher.getOrCreateForDocument(document) flatMap { watcher =>
              (watcher ? Listen(user, document)).mapTo[Enumerator[DocumentUpdate]] map { out =>
                val in = Iteratee.foreach[JsValue] { message =>
                  watcher ! message.as[DocumentUpdate]
                }

                (in, out &> updateSerializer)
              }
            }
          }.getOrElse(Future.successful(wsError("Not Found")))
        }
      }.getOrElse(Future.successful(wsError("Forbidden")))
    }
  }

  def wsError(message: String) = {
    (Iteratee.ignore[JsValue], Enumerator[JsValue](Json.obj("message" -> message)).andThen(Enumerator.eof))
  }
}
