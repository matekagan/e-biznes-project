package repositories

import java.sql.Timestamp

import javax.inject.{Inject, Singleton}
import models.Opinion
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OpinionRepository @Inject()(productRepository: ProductRepository, dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class OpinionTable(tag: Tag) extends Table[Opinion](tag, "opinions") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def product = column[Int]("product")

    def rating = column[Int]("rating")

    def timestamp = column[Timestamp]("timestamp", SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

    def comment = column[String]("comment")

    def productFK = foreignKey("product_fk", product, products)(_.id)

    override def * = (id, product, rating, comment, timestamp) <> ((Opinion.apply _).tupled, Opinion.unapply)
  }

  import productRepository.ProductTable

  private val opinion = TableQuery[OpinionTable]
  private val products = TableQuery[ProductTable]

  def create(product: Int, rating: Int, comment: String): Future[Opinion] = db.run {
    (opinion.map(p => (p.product, p.rating, p.comment, p.timestamp))
      returning opinion.map(_.id)
      into { case ((product, rating, comment, timestamp), id) => Opinion(id, product, rating, comment, timestamp) }
      ) += (product, rating, comment, new Timestamp(System.currentTimeMillis()))
  }

  def list(): Future[Seq[Opinion]] = db.run {
    opinion.result
  }

  def getByID(id: Int): Future[Opinion] = db.run {
    opinion.filter(_.id === id).result.head
  }

  def getByIDOption(id: Int): Future[Option[Opinion]] = db.run {
    opinion.filter(_.id === id).result.headOption
  }

  def getByProductID(id: Int): Future[Seq[Opinion]] = db.run {
    opinion.filter(_.product === id).result
  }

  def delete(id: Int): Future[Unit] = db.run(opinion.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, new_opinion: Opinion): Future[Unit] = {
    val opinionToUpdate = new_opinion.copy(id)
    db.run(opinion.filter(_.id === id).update(opinionToUpdate)).map(_ => ())
  }

}