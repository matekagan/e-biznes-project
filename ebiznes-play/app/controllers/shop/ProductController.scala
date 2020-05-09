package controllers.shop

import javax.inject.{Inject, Singleton}
import models.Product
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import repositories.{CategoryRepository, ProductRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductController @Inject()(productsRepo: ProductRepository, categoryRepo: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def newProductForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> number,
      "price" -> number
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  val updatingProductForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> number,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> number,
      "price" -> number
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def createProductForm: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepo.list()
    categories.map(cat => Ok(views.html.products.productForm(newProductForm, cat)))
  }

  def createProductHandle: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    categoryRepo.list() flatMap { categ =>
      newProductForm.bindFromRequest.fold(
        errorForm => {
          Future.successful(
            BadRequest(views.html.products.productForm(errorForm, categ))
          )
        },
        product => {
          productsRepo.create(product.name, product.description, product.category, product.price).map { _ =>
            Redirect(routes.ProductController.viewProducts()).flashing("success" -> "product created")
          }
        }
      )
    }
  }

  def updateProductForm(id: Int): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    categoryRepo.list() flatMap { categ =>
      val product = productsRepo.getById(id)
      product.map(product => {
        val prodForm = updatingProductForm.fill(UpdateProductForm(product.id, product.name, product.description, product.category, product.price))
        Ok(views.html.products.productUpdate(prodForm, categ))
      })
    }
  }

  def updateProductHandle = Action.async { implicit request =>
    categoryRepo.list() flatMap { categ =>
      updatingProductForm.bindFromRequest.fold(
        errorForm => {
          Future.successful(
            BadRequest(views.html.products.productUpdate(errorForm, categ))
          )
        },
        product => {
          productsRepo.update(product.id, Product(product.id, product.name, product.description, product.category, product.price)).map { _ =>
            Redirect(routes.ProductController.viewProducts()).flashing("success" -> "product updated")
          }
        }
      )
    }
  }

  def deleteProduct(id: Int) = Action.async { implicit request =>
    productsRepo.delete(id).flatMap(_ => productsRepo.list())
      .map(list => Ok(views.html.products.productsView(list)).flashing("success" -> "product deleted"))
  }

  def listProducts: Action[AnyContent] = Action.async { implicit request =>
    productsRepo.listWithDiscount()
      .map(v => v.map(_.getProductWithDiscount))
      .map(v => Json.toJson(v))
      .map(jsval => Ok(jsval))
  }

  def listProductsByCategory(id: Int) = Action.async { implicit request =>
    productsRepo.getByCategory(id)
      .map(list => Json.toJson(list))
      .map(json => Ok(json))
  }

  def getProduct(id: Int): Action[AnyContent] = Action.async { implicit request =>
    productsRepo.getByIdOption(id) map {
      case Some(product) => Ok(Json.toJson(product))
      case None => NotFound("product not found")
    }
  }

  def search(name: Option[String]) = Action.async { request =>
    name.filter(str => !str.isEmpty)
      .map(productsRepo.search)
      .map(result => result.map(res => Json.toJson(res.map(_.getProductWithDiscount))).map(jsVal => Ok(jsVal)))
      .getOrElse(Future.successful(Ok(Json.toJson(Seq[Product]()))))
  }

  def listWithDiscounts(ids: List[Int]) = Action.async {
    productsRepo.getProductsWithDiscount(ids)
      .map(v => v.map(_.getProductWithDiscount))
      .map(v => Json.toJson(v))
      .map(jsval => Ok(jsval))
  }

  def viewProducts() = Action.async { implicit request =>
    productsRepo.list()
      .map(products => Ok(views.html.products.productsView(products)))
  }
}

case class CreateProductForm(name: String, description: String, category: Int, price: Int)

case class UpdateProductForm(id: Int, name: String, description: String, category: Int, price: Int)