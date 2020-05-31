package auth.repository

import auth.model.User
import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.Future

trait UserRepository {

  def find(loginInfo: LoginInfo): Future[Option[User]]

  def find(userID: Int): Future[Option[User]]

  def save(user: User): Future[User]
}
