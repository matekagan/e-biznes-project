package auth

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import javax.inject.Inject
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future

class CustomSecuredErrorHandler @Inject()() extends SecuredErrorHandler {

  override def onNotAuthenticated(implicit request: RequestHeader) = {
    Future.successful(Forbidden("Forbidden"))
  }

  override def onNotAuthorized(implicit request: RequestHeader) = {
    Future.successful(Unauthorized("Unauthorized"))
  }
}