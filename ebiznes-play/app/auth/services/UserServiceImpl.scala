package auth.services

import auth.model
import auth.model.User
import auth.repository.UserRepository
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject()(userDAO: UserRepository)(implicit ex: ExecutionContext) extends UserService {

  def retrieve(id: Int) = userDAO.find(id)

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  def save(user: User) = userDAO.save(user)

  def save(profile: CommonSocialProfile) = {
    val name = UserServiceImpl.profileName(profile)
      userDAO.find(profile.loginInfo).flatMap {
        case Some(user) =>
          userDAO.save(user.copy(
            firstName = name._1,
            lastName = name._2,
            email = profile.email.getOrElse("")
          ))
        case None =>
          userDAO.save(model.User(
            provider = profile.loginInfo.providerID,
            identifier = profile.loginInfo.providerKey,
            firstName = name._1,
            lastName = name._2,
            email = profile.email.getOrElse("")
          ))
      }
  }
}

object UserServiceImpl {

  def profileName(profile: CommonSocialProfile): (Option[String], Option[String]) = profile.firstName.flatMap(
    fName => profile.lastName.map(lName => (Some(fName), Some(lName)))
  ).orElse(fullNameParsed(profile))
    .getOrElse((None, None))

  def fullNameParsed(profile: CommonSocialProfile): Option[(Option[String], Option[String])] = profile.fullName.map(
    name => if (name.contains(" ")) {
      val index = name.indexOf(" ")
      (Some(name.substring(0, index)), Some(name.substring(index + 1)))
    } else (Some(name), None)
  )

}
