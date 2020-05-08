package repositories

import javax.inject.{Inject, Singleton}
import models.Return
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnRepository  @Inject()(ordersRepository: OrdersRepository, dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class ReturnTable(tag: Tag) extends Table[Return](tag, "returns") {
    def id = column[Int]("id", O.PrimaryKey)
    def reason = column[String]("reason")
    def status = column[String]("status")

    def orderFK = foreignKey("order_fk", id, orders)(_.id)

    override def * = (id, status, reason) <> ((Return.apply _).tupled, Return.unapply)
  }

  import ordersRepository.OrdersTable

  private val returns = TableQuery[ReturnTable]
  private val orders =  TableQuery[OrdersTable]

  def create(order: Int, reason:String): Future[Unit] = db.run {
    (returns += Return(order, "new", reason)).map(_ => ())
  }

  def create(order: Int, reason:String, status: String): Future[Unit] = db.run {
    (returns += Return(order, status, reason)).map(_ => ())
  }

  def list(): Future[Seq[Return]] = db.run {
    returns.result
  }

  def getByIDOption(id: Int): Future[Option[Return]] = db.run {
    returns.filter(_.id === id).result.headOption
  }

  def delete(id: Int): Future[Unit] = db.run(returns.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, newReturn: Return): Future[Unit] = {
    val returnToUpdate = newReturn.copy(id)
    db.run(returns.filter(_.id === id).update(returnToUpdate)).map(_ => ())
  }

}