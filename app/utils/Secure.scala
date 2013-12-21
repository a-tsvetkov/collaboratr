package utils

import scala.concurrent._, ExecutionContext.Implicits.global
import play.api._, mvc._
import models.User
import controllers._

class AuthenticatedRequest[A](
  val user: User,
  request: Request[A]) extends WrappedRequest[A](request)

trait Secure {
  self: Controller =>

  object SecuredAction extends ActionBuilder[AuthenticatedRequest] {

    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[SimpleResult]) = {
      self.getUser(request) flatMap {
        _.map { user: User =>
          block(new AuthenticatedRequest(user, request))
        }.getOrElse(notAuthenticated)
      }
    }
  }

  def getUser(request: RequestHeader): Future[Option[User]] = {
    request.session.get("user_id").map { userId: String =>
      User.getById(userId.toLong)
    }.getOrElse(Future.successful(None))
  }

  def notAuthenticated = Future.successful(Redirect(routes.Security.login))
}
