package controllers.auth

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.ChangePasswordForm
import javax.inject.Inject
import play.api.mvc.AnyContent
import utils.auth.{DefaultEnv, WithProvider}
import views.html.auth.changePassword

import scala.concurrent.{ExecutionContext, Future}

class ChangePasswordController @Inject() (
  scc: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /**
   * Views the `Change Password` page.
   *
   * @return The result to display.
   */
  def view = SecuredAction(WithProvider[AuthType](CredentialsProvider.ID)) {
    implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
      Ok(changePassword(ChangePasswordForm.form))
  }

  /**
   * Changes the password.
   *
   * @return The result to display.
   */
  def submit = SecuredAction(WithProvider[AuthType](CredentialsProvider.ID)).async {
    implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
      ChangePasswordForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(changePassword(form))),
        password => {
          val (currentPassword, newPassword) = password
          val credentials = Credentials(request.identity.email, currentPassword)
          credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
            val passwordInfo = passwordHasherRegistry.current.hash(newPassword)
            authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
              Redirect(controllers.auth.routes.ChangePasswordController.view()).flashing("success" -> "Password Changed")
            }
          }.recover {
            case _: ProviderException =>
              Redirect(controllers.auth.routes.ChangePasswordController.view()).flashing("error" -> "Invalid Password")
          }
        }
      )
  }
}
