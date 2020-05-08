package repositories

import api.ProductWithDiscount
import javax.inject.{Inject, Singleton}
import models.{Discount, Product}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider, val categoryRepository: CategoryRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, "product") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def description = column[String]("description")

    def category = column[Int]("category")

    def price = column[Int]("price")

    def category_fk = foreignKey("category_fk", category, cat)(_.id)

    def * = (id, name, description, category, price) <> ((Product.apply _).tupled, Product.unapply)

  }

  class DiscountTable(tag: Tag) extends Table[Discount](tag, "discounts") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def rate = column[Int]("discount")

    def productFK = foreignKey("product_fk", id, products)(_.id)

    override def * = (id, rate) <> ((Discount.apply _).tupled, Discount.unapply)
  }

  import categoryRepository.CategoryTable

  private val products = TableQuery[ProductTable]
  private val cat = TableQuery[CategoryTable]
  private val discounts = TableQuery[DiscountTable]


  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(name: String, description: String, category: Int, price:Int): Future[Product] = db.run {
    (products.map(p => (p.name, p.description, p.category, p.price))
      returning products.map(_.id)
      into { case ((name, description, category, price), id) => Product(id, name, description, category, price) }
      ) += (name, description, category, price)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Product]] = db.run {
    products.result
  }

  def listWithDiscount() = db.run(tupledProductJoin.result).map(_.map(ProductWithDiscountTupled.tupled))

  def getByCategory(categoryID: Int): Future[Seq[Product]] = db.run {
    products.filter(_.category === categoryID).result
  }

  def getById(id: Int): Future[Product] = db.run {
    products.filter(_.id === id).result.head
  }

  def getByIdOption(id: Int): Future[Option[Product]] = db.run {
    products.filter(_.id === id).result.headOption
  }

  def getByCategories(categoryIDs: List[Int]): Future[Seq[Product]] = db.run {
    products.filter(_.category inSet categoryIDs).result
  }

  def delete(id: Int): Future[Unit] = db.run(products.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, newProduct: Product): Future[Unit] = {
    val productToUpdate: Product = newProduct.copy(id)
    db.run(products.filter(_.id === id).update(productToUpdate)).map(_ => ())
  }

  def search(searchText: String): Future[Seq[ProductWithDiscountTupled]] = {
    val expression = "%%%s%%".format(searchText.toLowerCase)
    db.run(tupledProductJoin.filter(prod => prod._1.name.toLowerCase like expression).result)
      .map(_.map(ProductWithDiscountTupled.tupled))
  }

  val tupledProductJoin: Query[(ProductTable, Rep[Option[DiscountTable]]), (Product, Option[Discount]), Seq] = products joinLeft discounts on (_.id === _.id)
  case class ProductWithDiscountTupled(product: Product, discount: Option[Discount]) {
    def getDiscountedProduct: Product = {
      val newPrice = product.price - discount.map(_.discount).getOrElse(0)
      product.copy(price = newPrice)
    }

    def getProductWithDiscount = ProductWithDiscount(product, discount)
  }

  def getProductsWithDiscount(productIDs: Seq[Int]) = {
    db.run(tupledProductJoin.filter(v => v._1.id inSet productIDs).result).map(_.map(ProductWithDiscountTupled.tupled))
  }
}
