package auth.repository

import auth.model.AuthToken

import scala.concurrent.Future

trait AuthTokenRepository {

  def find(id: Int): Future[Option[AuthToken]]

  def save(userID: Int): Future[AuthToken]

  def remove(id: Int): Future[Unit]
}
