package auth.repository

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class PasswordInfoDaoImpl @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, implicit val classTag: ClassTag[PasswordInfo]) extends DelegableAuthInfoDAO[PasswordInfo] {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class PasswordInfoDTOTable(tag: Tag) extends Table[PasswordInfoDTO](tag, "password_info") {
    def provider = column[String]("provider")

    def identifier = column[String]("identifier")

    def hasher = column[String]("hasher")

    def password = column[String]("password")

    def salt = column[Option[String]]("salt")

    def * = (provider, identifier, hasher, password, salt) <> (t => PasswordInfoDTO(t._1, t._2, t._3, t._4, t._5), PasswordInfoDTO.unapply)
  }

  val passwords = TableQuery[PasswordInfoDTOTable]

  private def filterByLoginIInfoQuery(loginInfo: LoginInfo) = passwords
    .filter(passwd => passwd.provider === loginInfo.providerID && passwd.identifier === loginInfo.providerKey)

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = db.run(
    filterByLoginIInfoQuery(loginInfo).result.headOption
  ).map(dto => dto.map(_.passwordInfo))

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = db.run (
    passwords += PasswordInfoDTO(loginInfo, authInfo)
  ).map(_ => authInfo)

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = db.run (
    filterByLoginIInfoQuery(loginInfo).update(PasswordInfoDTO(loginInfo, authInfo))
  ).map(_ => authInfo)

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = find(loginInfo).flatMap {
    case Some(_) => update(loginInfo, authInfo)
    case None => add(loginInfo, authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = db.run{
    filterByLoginIInfoQuery(loginInfo).delete.map(_ => ())
  }

}

case class PasswordInfoDTO(
  provider: String,
  identifier: String,
  hasher: String,
  password: String,
  salt: Option[String]
) {
  val loginInfo: LoginInfo = LoginInfo(provider, identifier)
  val passwordInfo: PasswordInfo = PasswordInfo(hasher, password, salt)
}

object PasswordInfoDTO {
  def apply(provider: String, identifier: String, hasher: String, password: String, salt: Option[String]): PasswordInfoDTO = new PasswordInfoDTO(provider, identifier, hasher, password, salt)

  def apply(loginInfo: LoginInfo, passwordInfo: PasswordInfo): PasswordInfoDTO = new PasswordInfoDTO(loginInfo.providerID, loginInfo.providerKey, passwordInfo.hasher, passwordInfo.password, passwordInfo.salt)
}