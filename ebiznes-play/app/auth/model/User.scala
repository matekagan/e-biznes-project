package auth.model

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

case class User(
  id: Int,
  provider: String,
  identifier: String,
  email: String,
  firstName: Option[String],
  lastName: Option[String]
) extends Identity {
  def loginInfo: LoginInfo = LoginInfo(provider, identifier)

  def name: Option[String] = firstName.flatMap(n => lastName.map(ln => "%s %s".format(n, ln)))
    .orElse(Some(identifier))
}

object User {
  def apply(id: Int, provider: String, identifier: String, email: String, firstName: Option[String], lastName: Option[String]): User = new User(id, provider, identifier, email, firstName, lastName)

  def apply(provider: String, identifier: String, email: String, firstName: Option[String], lastName: Option[String]): User = new User(-1, provider, identifier, email, firstName, lastName)
}
