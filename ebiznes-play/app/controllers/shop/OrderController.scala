package controllers.shop

import java.sql.Timestamp

import api.{CreateResult, SimpleOrderRepresentation}
import javax.inject.{Inject, Singleton}
import models.Order
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repositories.{DiscountRepository, OrdersRepository, ProductRepository}
import utils.TimeUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(ordersRepository: OrdersRepository, productRepository: ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def updatingOrderForm: Form[OrderUpdateForm] = Form {
    mapping(
      "id" -> number,
      "createDate" -> nonEmptyText,
      "homeDelivery" -> boolean,
      "address" -> nonEmptyText,
      "value" -> number,
      "status" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> nonEmptyText,
      "phone" -> nonEmptyText
    )(OrderUpdateForm.apply)(OrderUpdateForm.unapply)
  }

  def createOrder = Action.async { implicit request =>
    val orderRepresentationOption = request.body.asJson.map(Json.fromJson[SimpleOrderRepresentation](_))
    orderRepresentationOption.map(jsVal => jsVal.fold(
      _ => Future(BadRequest(Json.toJson(CreateResult()))),
      order => calculateOrderValue(order).flatMap(
        orderValue => ordersRepository.create(order, orderValue).map(uuid => Ok(Json.toJson(CreateResult(uuid.toString))))
      )
    )
    ).getOrElse(Future(BadRequest(Json.toJson(CreateResult()))))
  }

  def updateOrderForm(id: Int) = Action.async { implicit request =>
    val orderOption = ordersRepository.getByIDOption(id)
    orderOption.map {
      order =>
        order.map(value => updatingOrderForm.fill(OrderUpdateForm(value)))
          .map(form => Ok(views.html.orders.updateOrder(form)))
          .getOrElse(Ok("failed xD"))
    }
  }

  def updateOrderHandle() = Action.async { implicit request =>
    updatingOrderForm.bindFromRequest.fold(
      errors => Future.successful(Ok(views.html.orders.updateOrder(errors))),
      order => {
        ordersRepository.update(order.id, order.getOrder).map { _ =>
          Redirect(routes.OrderController.viewOrders()).flashing("success" -> "order updated")
        }
      }
    )
  }

  def deleteOrder(id: Int) = Action.async {
    ordersRepository.delete(id).map(_ => Redirect(routes.OrderController.viewOrders()))
  }

  def listOrders = Action.async {
    ordersRepository.list().map(list => Ok(Json.toJson(list)))
  }

  def getOrder(id: Int) = Action.async {
    ordersRepository.getByIDOption(id) map {
      case Some(product) => Ok(Json.toJson(product))
      case None => NotFound("order not found")
    }
  }

  def viewOrders() = Action.async { implicit requets =>
    ordersRepository.list()
      .map(list => Ok(views.html.orders.ordersView(list)))
  }

  private def calculateOrderValue(simpleOrderRepresentation: SimpleOrderRepresentation) = {
    val productAmounts = simpleOrderRepresentation.orderProducts.map(op => op.productID -> op.amount).toMap
    productRepository.getProductsWithDiscount(simpleOrderRepresentation.orderProducts.map(_.productID))
      .map {
        list => list.map(_.getDiscountedProduct).map(prod => prod.price * productAmounts.getOrElse(prod.id, 1)).sum
      }
  }
}

case class OrderUpdateForm(id: Int, createDate: String, homeDelivery: Boolean, address: String, value: Int, status: String, firstName: String, lastName: String, email: String, phone: String) {
  def getOrder = Order(id, TimeUtils.parseDate(createDate), homeDelivery, address, value, status, firstName, lastName, email, phone)
}

object OrderUpdateForm {
  def apply(id: Int, createDate: String, homeDelivery: Boolean, address: String, value: Int, status: String, firstName: String, lastName: String, email: String, phone: String):
  OrderUpdateForm = new OrderUpdateForm(id, createDate, homeDelivery, address, value, status, firstName, lastName, email, phone)

  def apply(order: Order): OrderUpdateForm =
    new OrderUpdateForm(order.id, TimeUtils.formatDate(order.createDate), order.homeDelivery, order.address, order.value, order.status, order.firstName, order.lastName, order.email, order.phone)
}