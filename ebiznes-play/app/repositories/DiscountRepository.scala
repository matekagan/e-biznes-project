package repositories

import javax.inject.{Inject, Singleton}
import models.Discount
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiscountRepository @Inject()(val productRepository: ProductRepository, val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  import productRepository.ProductTable
  import productRepository.DiscountTable

  private val discounts = TableQuery[DiscountTable]

  def createOrUpdate(id: Int, discount: Int) = {
    getByIDOption(id)
      .flatMap(
        disc => disc.map(_ => update(id, Discount(id, discount)))
          .getOrElse(insert(id, discount))
      )
  }

  def insert(id: Int, rate: Int) = {
    db.run(discounts += Discount(id, rate)).map(_ => ())
  }

  def list(): Future[Seq[Discount]] = db.run {
    discounts.result
  }

  def getByIDOption(id: Int): Future[Option[Discount]] = db.run {
    discounts.filter(_.id === id).result.headOption
  }

  def delete(id: Int): Future[Unit] = db.run(discounts.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, newDiscount: Discount): Future[Unit] = {
    val discountToUpdate = newDiscount.copy(id)
    db.run(discounts.filter(_.id === id).update(discountToUpdate)).map(_ => ())
  }
}