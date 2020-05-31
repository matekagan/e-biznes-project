package controllers.auth

import auth.model.User
import com.mohiva.play.silhouette.api.LoginEvent
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractAuthController(
  scc: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  protected def authenticateUser(user: User, result: Result)(implicit request: RequestHeader): Future[AuthenticatorResult] = {
    authenticatorService.create(user.loginInfo)
      .flatMap { authenticator =>
        eventBus.publish(LoginEvent(user, request))
        authenticatorService.init(authenticator).flatMap(authenticatorService.embed(_, result))
      }
  }
}
