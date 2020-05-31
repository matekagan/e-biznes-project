package controllers.auth

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import forms.SignInForm
import javax.inject.Inject
import play.api.mvc.{AnyContent, Request}
import utils.CommonCalls
import views.html.auth.signIn

import scala.concurrent.{ExecutionContext, Future}

class SignInController @Inject()(
  scc: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends AbstractAuthController(scc) {


  def view = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(signIn(SignInForm.form)))
  }

  def submit = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(signIn(form))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) => authenticateUser(user, Redirect(CommonCalls.home))
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException => Redirect(CommonCalls.signin)
        }
      }
    )
  }
}

