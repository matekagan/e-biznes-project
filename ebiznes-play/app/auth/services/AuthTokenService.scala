package auth.services

import auth.model.AuthToken

import scala.concurrent.Future
import scala.language.postfixOps

trait AuthTokenService {

  def create(userID: Int): Future[AuthToken]

  def validate(id: Int): Future[Option[AuthToken]]
}
