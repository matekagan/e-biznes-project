package repositories

import java.sql.Timestamp

import auth.model.User
import auth.repository.{UserRepositoryImpl}
import javax.inject.{Inject, Singleton}
import models.Opinion
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OpinionRepository @Inject()(
  productRepository: ProductRepository,
  dbConfigProvider: DatabaseConfigProvider,
  val userRepository: UserRepositoryImpl
)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class OpinionTable(tag: Tag) extends Table[Opinion](tag, "opinions") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def product = column[Int]("product")

    def rating = column[Int]("rating")

    def timestamp = column[Timestamp]("timestamp", SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

    def comment = column[String]("comment")

    def user = column[Int]("user")

    def productFK = foreignKey("product_fk", product, products)(_.id)

    def userFK = foreignKey("user", product, products)(_.id)

    override def * = (id, product, rating, comment, timestamp, user) <> ((Opinion.apply _).tupled, Opinion.unapply)
  }

  import productRepository.ProductTable
  import userRepository.UserTable

  private val opinions = TableQuery[OpinionTable]
  private val products = TableQuery[ProductTable]
  private val users = TableQuery[UserTable]

  def create(product: Int, rating: Int, comment: String, userID: Int): Future[Opinion] = db.run {
    (opinions.map(p => (p.product, p.rating, p.comment, p.timestamp, p.user))
      returning opinions.map(_.id)
      into { case ((product, rating, comment, timestamp, user), id) => Opinion(id, product, rating, comment, timestamp, user) }
      ) += (product, rating, comment, new Timestamp(System.currentTimeMillis()), userID)
  }

  def list(): Future[Seq[Opinion]] = db.run {
    opinions.result
  }

  def getByID(id: Int): Future[Opinion] = db.run {
    opinions.filter(_.id === id).result.head
  }

  def getByIDOption(id: Int): Future[Option[Opinion]] = db.run {
    opinions.filter(_.id === id).result.headOption
  }

  def getByProductID(id: Int): Future[Seq[(Opinion, User)]] = db.run {
    (for {
      opinion <- opinions if opinion.id === id
      user <- users if opinion.user === user.id
    } yield (opinion, user)).result
  }

  def delete(id: Int): Future[Unit] = db.run(opinions.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, newOpinion: Opinion): Future[Unit] = {
    val opinionToUpdate = newOpinion.copy(id)
    db.run(opinions.filter(_.id === id).update(opinionToUpdate)).map(_ => ())
  }

}