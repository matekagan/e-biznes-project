package controllers.shop

import javax.inject.{Inject, Singleton}
import models.Payment
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repositories.PaymentRepository
import utils.TimeUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentController @Inject()(paymentRepository: PaymentRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def updatingPaymentForm: Form[UpdatePaymentForm] = Form {
    mapping(
      "id" -> number,
      "order" -> number,
      "value" -> number,
      "createdTime" -> nonEmptyText,
      "status" -> nonEmptyText
    )(UpdatePaymentForm.apply)(UpdatePaymentForm.unapply)
  }

  def creatingPaymentForm: Form[CreatePaymentForm] = Form {
    mapping(
      "value" -> number,
      "status" -> nonEmptyText
    )(CreatePaymentForm.apply)(CreatePaymentForm.unapply)
  }


  def createPaymentForm(order: Int) = Action { implicit request =>
    Ok(views.html.payments.createPayment(creatingPaymentForm, order))
  }

  def createPayment(order: Int) = Action.async { implicit request =>
    creatingPaymentForm.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.payments.createPayment(errorForm, order))),
      payment => paymentRepository.create(order, payment.value, payment.status).map { _ =>
        Redirect(routes.PaymentController.viewPayments()).flashing("success" -> "payment created")
      }
    )
  }

  def updatePaymentForm(id: Int) = Action.async { implicit request =>
    val paymentOption = paymentRepository.getByIDOption(id)
    paymentOption.map {
      payment =>
        payment.map(value => updatingPaymentForm.fill(UpdatePaymentForm(value)))
          .map(form => Ok(views.html.payments.updatePayment(form)))
          .getOrElse(Ok("payment not found"))
    }
  }

  def updatePaymentHandle = Action.async { implicit request =>
    updatingPaymentForm.bindFromRequest.fold(
      errors => Future.successful(Ok(views.html.payments.updatePayment(errors))),
      payment => {
        paymentRepository.update(payment.id, payment.getPayment).map { _ =>
          Redirect(routes.PaymentController.viewPayments()).flashing("success" -> "payment updated")
        }
      }
    )
  }

  def deletePayment(id: Int) = Action.async {
    paymentRepository.delete(id).map(_ => Redirect(routes.PaymentController.viewPayments()).flashing("success" -> "Payment deleted"))
  }

  def listPayments = Action.async {
    paymentRepository.list()
      .map(list => Json.toJson(list))
      .map(jsVal => Ok(jsVal))
  }

  def getPayment(id: Int) = Action.async {
    paymentRepository.getByIDOption(id) map {
      case Some(payment) => Ok(Json.toJson(payment))
      case None => NotFound("Category not found")
    }
  }

  def viewPayments() = Action.async { implicit request =>
    paymentRepository.list()
      .map(list => Ok(views.html.payments.paymentsView(list)))
  }
}

case class UpdatePaymentForm(id: Int, order: Int, value: Int, createdTime: String, status: String) {
  def getPayment: Payment = Payment(id, order, value, TimeUtils.parseDate(createdTime), status)
}

case class CreatePaymentForm(value: Int, status: String)

object UpdatePaymentForm {
  def apply(id: Int, order: Int, value: Int, createdTime: String, status: String): UpdatePaymentForm = new UpdatePaymentForm(id, order, value, createdTime, status)

  def apply(payment: Payment): UpdatePaymentForm = new UpdatePaymentForm(payment.id, payment.order, payment.value, TimeUtils.formatDate(payment.createdTime), payment.status)
}