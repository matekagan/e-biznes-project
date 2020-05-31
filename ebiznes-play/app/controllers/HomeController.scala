package controllers


import com.mohiva.play.silhouette.api.LogoutEvent
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import auth.{SilhouetteController, SilhouetteControllerComponents}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject._
import play.api.mvc.{AnyContent, DiscardingCookie}
import utils.CommonCalls
import utils.auth.{DefaultEnv, WithProvider}

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: SilhouetteControllerComponents)(implicit ec: ExecutionContext) extends SilhouetteController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def adminPanel = SecuredAction(WithProvider[AuthType](CredentialsProvider.ID)) {
    implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Ok(views.html.adminPanel())
  }

  def signOut = SecuredAction.async { implicit request: SecuredRequest[EnvType, AnyContent] =>
    val result = Redirect(CommonCalls.home)
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
      .map(_.discardingCookies(
        DiscardingCookie(name = "csrfToken"),
        DiscardingCookie(name = "PLAY_SESSION"),
        DiscardingCookie(name = "OAuth2-authenticator"),
        DiscardingCookie(name = "authenticator")
      ))
  }

}
