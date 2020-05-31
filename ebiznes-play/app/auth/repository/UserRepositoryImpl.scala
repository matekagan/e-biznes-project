package auth.repository

import auth.model.User
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserRepositoryImpl @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserRepository {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def provider = column[String]("provider")

    def identifier = column[String]("identifier")

    def email = column[String]("email")

    def firstName = column[Option[String]]("first_name")

    def lastName = column[Option[String]]("last_name")

    def * = (id, provider, identifier, email, firstName, lastName) <> (t => User(t._1, t._2, t._3, t._4, t._5, t._6), User.unapply)
  }

  val users = TableQuery[UserTable]

  override def find(loginInfo: LoginInfo): Future[Option[User]] = db.run {
    users.filter(user => user.provider === loginInfo.providerID && user.identifier === loginInfo.providerKey)
      .result.headOption
  }

  override def find(userID: Int): Future[Option[User]] = db.run {
    users.filter(_.id === userID).result.headOption
  }

  override def save(newUser: User): Future[User] = find(newUser.id).flatMap {
    case Some(_) => update(newUser)
    case None => create(newUser)
  }

  def update(user: User): Future[User] = db.run(users.filter(_.id === user.id).update(user))
    .map(_ => user)

  def create(user: User): Future[User] = db.run {
    (users.map(p => (p.provider, p.identifier, p.email, p.firstName, p.lastName))
      returning users.map(_.id)
      into { case ((provider, identifier, email, firstName, lastName), id) => User(id, provider, identifier, email, firstName, lastName) }
      ) += (user.provider, user.identifier, user.email, user.firstName, user.lastName)
  }
}
