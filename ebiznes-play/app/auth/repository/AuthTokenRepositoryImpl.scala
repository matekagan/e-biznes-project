package auth.repository

import auth.model.AuthToken
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class AuthTokenRepositoryImpl @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends AuthTokenRepository {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class AuthTokenTable(tag: Tag) extends Table[AuthToken](tag, "tokens") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userID = column[Int]("userID")

    override def * = (id, userID) <> ((AuthToken.apply _).tupled, AuthToken.unapply)
  }

  private val tokens = TableQuery[AuthTokenTable]

  override def find(id: Int): Future[Option[AuthToken]] = db.run(tokens.filter(_.id === id).result.headOption)

  override def save(userID : Int): Future[AuthToken] = db.run {
    (tokens.map(c => (c.userID))
      returning tokens.map(_.id)
      into ((id, userID) => AuthToken(id, userID))
      ) += (userID)
  }

  override def remove(id: Int): Future[Unit] = db.run {
    tokens.filter(_.id === id).delete.map(_ => ())
  }
}