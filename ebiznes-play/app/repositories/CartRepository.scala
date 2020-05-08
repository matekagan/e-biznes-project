package repositories

import java.sql.Timestamp
import java.util.UUID

import api.ProductWithAmount
import javax.inject.{Inject, Singleton}
import models.{Cart, CartProduct, Discount, Product}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartRepository @Inject()(val productRepository: ProductRepository, val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CartTable(tag: Tag) extends Table[Cart](tag, "carts") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def uuid = column[String]("uuid")

    def timeStamp = column[Timestamp]("timestamp")

    def * = (id, uuid, timeStamp) <> ((Cart.apply _).tupled, Cart.unapply)
  }

  class CartProductsTable(tag: Tag) extends Table[CartProduct](tag, "cart_products") {
    def cartID = column[Int]("cart_id")

    def productID = column[Int]("product_id")

    def amount = column[Int]("amount")

    def cartFK = foreignKey("cart_fk", cartID, carts)(_.id, onDelete = ForeignKeyAction.Cascade)

    def productFK = foreignKey("product_fk", productID, products)(_.id, onDelete = ForeignKeyAction.Cascade)

    def orderProductPK = primaryKey("pk_orders_products", (cartID, productID))

    override def * = (cartID, productID, amount) <> ((CartProduct.apply _).tupled, CartProduct.unapply)
  }

  import productRepository.ProductTable
  import productRepository.DiscountTable

  val carts = TableQuery[CartTable]
  val cartProducts = TableQuery[CartProductsTable]
  private val products = TableQuery[ProductTable]
  private val discounts = TableQuery[DiscountTable]

  def create(newCartProducts: Seq[ProductWithAmount]) = {
    val uuid = UUID.randomUUID().toString
    val futureCartID = db.run(
      (carts.map(c => (c.uuid, c.timeStamp))
        returning carts.map(_.id)
        into { case ((uuid, timestamp), id) => Cart(id, uuid, timestamp) }
        ) += (uuid, new Timestamp(System.currentTimeMillis()))
    )
    futureCartID
      .flatMap(cartCreated => db.run(cartProducts ++= createCartProducts(cartCreated.id, newCartProducts)))
      .map(_ => uuid)
  }

  def delete(uuid: String) = db.run(carts.filter(_.uuid === uuid).delete)
    .flatMap(cartID => db.run(cartProducts.filter(_.cartID === cartID).delete))
    .map(_ => ())

  def delete(id: Int) = db.run(carts.filter(_.id === id).delete)
    .flatMap(cartID => db.run(cartProducts.filter(_.cartID === cartID).delete))
    .map(_ => ())

  def getByIDOption(id: Int) = db.run(carts.filter(_.id === id).result.headOption)

  def list() = db.run(carts.result)

  private def createCartProducts(cartID: Int, cartProducts: Seq[ProductWithAmount]) = {
    cartProducts.map(cp => CartProduct(cartID, cp.productID, cp.amount))
  }

  val tupledProductJoin: Query[((CartProductsTable, productRepository.ProductTable), Rep[Option[productRepository.DiscountTable]]), ((CartProduct, Product), Option[Discount]), Seq] =
    cartProducts join products on (_.productID === _.id) joinLeft discounts on (_._2.id === _.id)

  def getProductsWithAmount(cartUUID: String): Future[Seq[((CartProduct, Product), Option[Discount])]] = {
    val futureCartOption = db.run(carts.filter(_.uuid === cartUUID).result.headOption)
    futureCartOption.flatMap(
      cartOption => cartOption.map(cart => db.run(tupledProductJoin.filter(_._1._1.cartID === cart.id).result))
        .getOrElse(Future.successful(Seq[((CartProduct, Product), Option[Discount])]()))
    )
  }

  case class ProductsWithAmount(cartProduct: (CartProduct, Product), discount: Option[Discount])

}
