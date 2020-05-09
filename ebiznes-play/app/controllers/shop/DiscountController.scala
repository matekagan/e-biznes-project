package controllers.shop

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repositories.{DiscountRepository, ProductRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiscountController @Inject()(discountRepository: DiscountRepository, productRepository: ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def discountForm: Form[DiscountForm] = Form {
    mapping(
      "product" -> number,
      "discount" -> number
    )(DiscountForm.apply)(DiscountForm.unapply)
  }

  def createDiscountForm = Action.async { implicit request =>
    productRepository.list()
      .map(products => Ok(views.html.discounts.createDiscount(discountForm, products)))
  }

  def createDiscountOrUpdate = Action.async { implicit request =>
    productRepository.list() flatMap { products =>
      discountForm.bindFromRequest().fold(
        error => Future.successful(BadRequest(views.html.discounts.createDiscount(error, products))),
        discount => discountRepository.createOrUpdate(discount.product, discount.discount).map { _ =>
          Redirect(routes.DiscountController.viewDiscounts()).flashing("success" -> "discount created or updated")
        }
      )
    }

  }

  def updateDiscountForm(id: Int) = Action.async { implicit request =>
    productRepository.list() flatMap { products =>
      val discountOption = discountRepository.getByIDOption(id)
      discountOption.map {
        discount =>
          discount.map(value => discountForm.fill(DiscountForm(value.id, value.discount)))
            .map(form => Ok(views.html.discounts.updateDiscount(form)))
            .getOrElse(Ok(views.html.discounts.createDiscount(discountForm, products)))
      }
    }
  }

  def deleteDiscount(id: Int) = Action.async {
    discountRepository.delete(id)
      .map(_ => Redirect(routes.DiscountController.viewDiscounts()))
  }

  def listDiscounts = Action.async {
    discountRepository.list()
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def getDiscount(id: Int) = Action.async { request =>
    discountRepository.getByIDOption(id)
      .map(discount => Ok(Json.toJson(discount)))
  }

  def viewDiscounts() = Action.async { implicit request =>
    discountRepository.list()
      .map(list => Ok(views.html.discounts.discountsView(list)))
  }
}

case class DiscountForm(product: Int, discount: Int)