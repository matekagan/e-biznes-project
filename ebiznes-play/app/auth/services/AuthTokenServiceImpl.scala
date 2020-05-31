package auth.services

import auth.repository.AuthTokenRepositoryImpl
import com.mohiva.play.silhouette.api.util.Clock
import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class AuthTokenServiceImpl @Inject()(
  authTokenDAO: AuthTokenRepositoryImpl,
  clock: Clock
)(
  implicit
  ex: ExecutionContext
) extends AuthTokenService {

  def create(userID: Int) = {
    authTokenDAO.save(userID)
  }

  def validate(id: Int) = authTokenDAO.find(id)

}
