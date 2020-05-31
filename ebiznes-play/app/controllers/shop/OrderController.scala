package controllers.shop

import api.{CreateResult, SimpleOrderRepresentation}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import controllers.auth.{SilhouetteController, SilhouetteControllerComponents}
import javax.inject.{Inject, Singleton}
import models.Order
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import repositories.{OrdersRepository, ProductRepository}
import utils.TimeUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(ordersRepository: OrdersRepository, productRepository: ProductRepository, cc: SilhouetteControllerComponents)(implicit ec: ExecutionContext) extends SilhouetteController(cc) {

  def updatingOrderForm: Form[OrderUpdateForm] = Form {
    mapping(
      "id" -> number,
      "createDate" -> nonEmptyText,
      "address" -> nonEmptyText,
      "value" -> number,
      "status" -> nonEmptyText,
      "phone" -> nonEmptyText,
      "userID" -> number
    )(OrderUpdateForm.apply)(OrderUpdateForm.unapply)
  }

  def createOrder = SecuredAction.async { implicit request: SecuredRequest[EnvType, AnyContent] =>
    val orderRepresentationOption = request.body.asJson.map(Json.fromJson[SimpleOrderRepresentation](_))
    val user = getAuthorizedUser(request)
    orderRepresentationOption.map(jsVal => jsVal.fold(
      _ => Future(BadRequest(Json.toJson(CreateResult()))),
      order => calculateOrderValue(order).flatMap(
        orderValue => user.flatMap(
          user => ordersRepository.create(order, orderValue, user.id)
            .map(uuid => Ok(Json.toJson(CreateResult(uuid.toString))))
        )
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

case class OrderUpdateForm(id: Int, createDate: String, address: String, value: Int, status: String, phone: String, userID: Int) {
  def getOrder = Order(id, TimeUtils.parseDate(createDate), address, value, status, phone, userID)
}

object OrderUpdateForm {
  def apply(id: Int, createDate: String, address: String, value: Int, status: String, phone: String, userID: Int):
  OrderUpdateForm = new OrderUpdateForm(id, createDate, address, value, status, phone, userID)

  def apply(order: Order): OrderUpdateForm =
    new OrderUpdateForm(order.id, TimeUtils.formatDate(order.createDate), order.address, order.value, order.status, order.phone, order.userID)
}