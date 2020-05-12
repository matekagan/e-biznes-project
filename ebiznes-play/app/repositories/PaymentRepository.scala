package repositories

import java.sql.Timestamp

import javax.inject.{Inject, Singleton}
import models.Payment
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentRepository @Inject()(ordersRepository: OrdersRepository, dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class PaymentTable(tag: Tag) extends Table[Payment](tag, "payments") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def order = column[Int]("order_id")

    def value = column[Int]("value")

    def status = column[String]("status")

    def createdAt = column[Timestamp]("created_time")

    def orderFK = foreignKey("order_fk", order, orders)(_.id)

    override def * = (id, order,  value, createdAt, status) <> ((Payment.apply _).tupled, Payment.unapply)
  }

  import ordersRepository.OrdersTable

  private val payment = TableQuery[PaymentTable]
  private val orders = TableQuery[OrdersTable]

  def create(order: Int, value: Int, status: String): Future[Payment] = db.run {
    (payment.map(p => (p.order, p.value, p.status, p.createdAt))
      returning payment.map(_.id)
      into {case ((order, value, status, createdTime), id) => Payment(id, order, value, createdTime, status)}
      ) += (order, value, status, new Timestamp(System.currentTimeMillis()))
  }

  def list(): Future[Seq[Payment]] = db.run {
    payment.result
  }

  def getByIDOption(id: Int): Future[Option[Payment]] = db.run {
    payment.filter(_.id === id).result.headOption
  }

  def delete(id: Int): Future[Unit] = db.run(payment.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, newPayment: Payment): Future[Unit] = {
    val paymentToUpdate = newPayment.copy(id)
    db.run(payment.filter(_.id === id).update(paymentToUpdate)).map(_ => ())
  }
}