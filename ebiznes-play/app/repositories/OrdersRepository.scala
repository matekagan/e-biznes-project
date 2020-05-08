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

    def homeDelivery = column[Boolean]("delivery")

    def address = column[String]("address")

    def value = column[Int]("value")

    def status = column[String]("status")

    def firstName = column[String]("first_name")

    def lastName = column[String]("last_name")

    def email = column[String]("e_mail")

    def phone = column[String]("phone")

    override def * = (id, createdDate, homeDelivery, address, value, status, firstName, lastName, email, phone) <> ((Order.apply _).tupled, Order.unapply)
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

  def create(order: SimpleOrderRepresentation, orderValue: Int) = {
    val futureOrderCreated = db.run((orders.map(p => (p.createdDate, p.homeDelivery, p.address, p.value, p.status, p.firstName, p.lastName, p.email, p.phone))
      returning orders.map(_.id)
      into { case ((createDate, homeDelivery, address, value, status, firstName, lastName, email, phone), id) => Order(id, createDate, homeDelivery, address, value, status, firstName, lastName, email, phone) }
      ) += (new Timestamp(System.currentTimeMillis()), order.homeDelivery, order.address, orderValue, "new", order.firstName, order.lastName, order.email, order.phone))
    futureOrderCreated.flatMap(orderCreated => db.run(orderProducts ++= order.getOrderProducts(orderCreated.id)).map(_ => orderCreated.id))
  }

  def delete(id: Int) = db.run(orders.filter(_.id === id).delete).map(_ => ())
    .flatMap(_ => db.run(orderProducts.filter(_.orderID === id).delete.map(_ => ())))

  def update(id: Int, newOrder: Order): Future[Unit] = {
    val orderToUpdate = newOrder.copy(id)
    db.run(orders.filter(_.id === id).update(orderToUpdate)).map(_ => ())
  }

  def getByIDOption(id: Int)  = db.run(orders.filter(_.id === id).result.headOption)

  def list() = db.run(orders.result)
}
