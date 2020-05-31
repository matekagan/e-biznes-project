package controllers.auth

import com.mohiva.play.silhouette.api.LoginEvent
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, SocialProvider}
import javax.inject.Inject
import play.api.mvc.{AnyContent, Cookie, Request}
import play.filters.csrf.CSRF
import play.filters.csrf.CSRF.Token
import utils.CommonCalls

import scala.concurrent.{ExecutionContext, Future}

class SocialAuthController @Inject() (
  scc: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def authenticate(provider: String) = Action.async { implicit request: Request[AnyContent] =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authToken <- authTokenService.create(user.id)
            authenticator <- authenticatorService.create(profile.loginInfo)
            value <- authenticatorService.init(authenticator)
            result <- authenticatorService.embed(value, Redirect(CommonCalls.webHome))
          } yield {
            val Token(name, value) = CSRF.getToken.get
            eventBus.publish(LoginEvent(user, request))
            result.withCookies(Cookie(name, value, httpOnly = false))
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(CommonCalls.signin).flashing("error" -> "error during authorization using social account")
    }
  }
}
