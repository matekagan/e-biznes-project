package auth.repository

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class OAuth2InfoDaoImpl @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, implicit val classTag: ClassTag[OAuth2Info]) extends DelegableAuthInfoDAO[OAuth2Info] {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class OAuth2InfoTable(tag: Tag) extends Table[OAuth2InfoDTO](tag, "oauth2_info") {
    def provider = column[String]("provider")

    def identifier = column[String]("identifier")

    def accessToken = column[String]("access_token")

    def tokenType = column[Option[String]]("token_type")

    def expiresIn = column[Option[Int]]("expires_in")

    def refreshToken = column[Option[String]]("refresh_token")

    def * = (provider, identifier, accessToken, tokenType, expiresIn, refreshToken) <> (t => OAuth2InfoDTO(t._1, t._2, t._3, t._4, t._5, t._6), OAuth2InfoDTO.unapply)
  }

  val authInfos = TableQuery[OAuth2InfoTable]

  private def filterByLoginIInfoQuery(loginInfo: LoginInfo) = authInfos
    .filter(auth => auth.provider === loginInfo.providerID && auth.identifier === loginInfo.providerKey)


  override def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = db.run (
    filterByLoginIInfoQuery(loginInfo).result.headOption
  ).map(a => a.map(_.authInfo))

  override def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = db.run(
    authInfos += OAuth2InfoDTO(loginInfo, authInfo)
  ).map(_ => authInfo)

  override def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = db.run(
    filterByLoginIInfoQuery(loginInfo).update(OAuth2InfoDTO(loginInfo, authInfo))
  ).map(_ => authInfo)

  override def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = find(loginInfo).flatMap {
    case Some(_) => update(loginInfo, authInfo)
    case None => add(loginInfo, authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = db.run {
    filterByLoginIInfoQuery(loginInfo).delete.map(_ => ())
  }
}


case class OAuth2InfoDTO(
  provider: String,
  identifier: String,
  accessToken: String,
  tokenType: Option[String],
  expiresIn: Option[Int],
  refreshToken: Option[String]
) {
  val loginInfo: LoginInfo = LoginInfo(provider, identifier)
  val authInfo: OAuth2Info = OAuth2Info(accessToken, tokenType, expiresIn, refreshToken)
}

object OAuth2InfoDTO {
  def apply(provider: String, identifier: String, accessToken: String, tokenType: Option[String], expiresIn: Option[Int], refreshToken: Option[String]): OAuth2InfoDTO = new OAuth2InfoDTO(provider, identifier, accessToken, tokenType, expiresIn, refreshToken)

  def apply(loginInfo: LoginInfo, oauthInfo: OAuth2Info): OAuth2InfoDTO = new OAuth2InfoDTO(loginInfo.providerID, loginInfo.providerKey, oauthInfo.accessToken, oauthInfo.tokenType, oauthInfo.expiresIn, oauthInfo.refreshToken)
}