package repositories

import java.sql.Timestamp

import api.SimpleOrderRepresentation
import javax.inject.{Inject, Singleton}
import models.{Order, OrderProduct}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrdersRepository @Inject()(val productRepository: ProductRepository, val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class OrdersTable(tag: Tag) extends Table[Order](tag, "orders") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def createdDate = column[Timestamp]("created_date")

    def address = column[String]("address")

    def value = column[Int]("value")

    def status = column[String]("status")

    def phone = column[String]("phone")

    def userID = column[Int]("user_id")

    override def * = (id, createdDate, address, value, status, phone, userID) <> ((Order.apply _).tupled, Order.unapply)
  }

  class OrderProductsTable(tag: Tag) extends Table[OrderProduct](tag, "orders_products") {
    def orderID = column[Int]("order_id")

    def productID = column[Int]("product_id")

    def amount = column[Int]("amount")

    def orderFK = foreignKey("order_fk", orderID, orders)(_.id, onDelete = ForeignKeyAction.Cascade)

    def productFK = foreignKey("product_fk", productID, products)(_.id, onDelete = ForeignKeyAction.Cascade)

    def orderProductPK = primaryKey("pk_orders_products", (orderID, productID))

    override def * = (orderID, productID, amount) <> ((OrderProduct.apply _).tupled, OrderProduct.unapply)
  }

  import productRepository.ProductTable

  private val orders = TableQuery[OrdersTable]
  private val orderProducts = TableQuery[OrderProductsTable]
  private val products = TableQuery[ProductTable]

  def create(order: SimpleOrderRepresentation, orderValue: Int, userID: Int) = {
    val futureOrderCreated = db.run((orders.map(p => (p.createdDate, p.address, p.value, p.status, p.phone, p.userID))
      returning orders.map(_.id)
      into { case ((createDate, address, value, status, phone, userID), id) => Order(id, createDate, address, value, status, phone, userID) }
      ) += (new Timestamp(System.currentTimeMillis()), order.address, orderValue, "new", order.phone, userID))
    futureOrderCreated.flatMap(orderCreated => db.run(orderProducts ++= order.getOrderProducts(orderCreated.id)).map(_ => orderCreated.id))
  }

  def delete(id: Int) = db.run(orders.filter(_.id === id).delete).map(_ => ())
    .flatMap(_ => db.run(orderProducts.filter(_.orderID === id).delete.map(_ => ())))

  def update(id: Int, newOrder: Order): Future[Unit] = {
    val orderToUpdate = newOrder.copy(id)
    db.run(orders.filter(_.id === id).update(orderToUpdate)).map(_ => ())
  }

  def getByIDOption(id: Int) = db.run(orders.filter(_.id === id).result.headOption)

  def list() = db.run(orders.result)
}
