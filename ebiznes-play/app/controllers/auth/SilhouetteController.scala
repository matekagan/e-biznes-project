package controllers.auth

import auth.services.{AuthTokenService, UserService}
import com.mohiva.play.silhouette.api.actions.{SecuredActionBuilder, SecuredRequest, UnsecuredActionBuilder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{EventBus, Silhouette}
import com.mohiva.play.silhouette.impl.providers.{CredentialsProvider, SocialProviderRegistry}
import javax.inject.Inject
import play.api.Logging
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

abstract class SilhouetteController(override protected val controllerComponents: SilhouetteControllerComponents)
  extends MessagesAbstractController(controllerComponents) with SilhouetteComponents with Logging {

  val SecuredAction: SecuredActionBuilder[EnvType, AnyContent] = controllerComponents.silhouette.SecuredAction
  val UnsecuredAction: UnsecuredActionBuilder[EnvType, AnyContent] = controllerComponents.silhouette.UnsecuredAction

  def getAuthorizedUser(request: SecuredRequest[EnvType, AnyContent])(implicit ec: ExecutionContext) = getAuthorizedUserOption(request)
    .map(_.get)

  def getAuthorizedUserOption(request: SecuredRequest[EnvType, AnyContent])(implicit ec: ExecutionContext) = authenticatorService.retrieve(request)
    .flatMap { auth =>
      val logInfo = auth.map(_.loginInfo)
      logInfo.map(userService.retrieve(_))
        .getOrElse(Future.successful(None))
    }

  def userService: UserService = controllerComponents.userService
  def authInfoRepository: AuthInfoRepository = controllerComponents.authInfoRepository
  def passwordHasherRegistry: PasswordHasherRegistry = controllerComponents.passwordHasherRegistry
  def authTokenService: AuthTokenService = controllerComponents.authTokenService
  def clock: Clock = controllerComponents.clock
  def credentialsProvider: CredentialsProvider = controllerComponents.credentialsProvider
  def socialProviderRegistry: SocialProviderRegistry = controllerComponents.socialProviderRegistry

  def silhouette: Silhouette[EnvType] = controllerComponents.silhouette
  def authenticatorService: AuthenticatorService[AuthType] = silhouette.env.authenticatorService
  def eventBus: EventBus = silhouette.env.eventBus
}
trait SilhouetteComponents {
  type EnvType = DefaultEnv
  type AuthType = EnvType#A
  type IdentityType = EnvType#I

  def userService: UserService

  def authInfoRepository: AuthInfoRepository

  def passwordHasherRegistry: PasswordHasherRegistry

  def authTokenService: AuthTokenService

  def clock: Clock

  def credentialsProvider: CredentialsProvider

  def socialProviderRegistry: SocialProviderRegistry

  def silhouette: Silhouette[EnvType]
}

trait SilhouetteControllerComponents extends MessagesControllerComponents with SilhouetteComponents

final case class DefaultSilhouetteControllerComponents @Inject() (
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry,
  authTokenService: AuthTokenService,
  clock: Clock,
  credentialsProvider: CredentialsProvider,
  messagesActionBuilder: MessagesActionBuilder,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  socialProviderRegistry: SocialProviderRegistry,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: scala.concurrent.ExecutionContext
) extends SilhouetteControllerComponents