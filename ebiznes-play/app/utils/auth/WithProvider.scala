package utils.auth

import auth.model.User
import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import play.api.mvc.Request

import scala.concurrent.Future

case class WithProvider[A <: Authenticator](providers: String*) extends Authorization[User, A] {

  override def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]
  ): Future[Boolean] = {
    Future.successful(providers.isEmpty || providers.contains(user.loginInfo.providerID))
  }
}
