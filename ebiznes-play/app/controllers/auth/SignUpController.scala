package controllers.auth

import auth.model.User
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignUpForm
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(
  components: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends SilhouetteController(components) {

  def view = UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.auth.signUp(SignUpForm.form)))
  }

  def submit = UnsecuredAction.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.signUp(form))),
      data => {
        val result = Redirect(routes.SignInController.view()).flashing("success" -> "Account Created")
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => Future.successful(result)
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            val newUser = User(
              loginInfo.providerID,
              loginInfo.providerKey,
              data.email,
              Some(data.firstName),
              Some(data.lastName)
            )
            for {
              user <- userService.save(newUser)
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authToken <- authTokenService.create(user.id)
            } yield {
              eventBus.publish(SignUpEvent(user, request))
              result
            }
        }
      }
    )
  }
}

