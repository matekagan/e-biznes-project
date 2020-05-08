package controllers.shop

import api.SimpleReturn
import javax.inject.{Inject, Singleton}
import models.Return
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repositories.ReturnRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnController @Inject()(returnRepository: ReturnRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def updatingReturnForm: Form[UpdateReturnForm] = Form {
    mapping(
      "order" -> number,
      "status" -> nonEmptyText,
      "reason" -> nonEmptyText
    )(UpdateReturnForm.apply)(UpdateReturnForm.unapply)
  }

  def creatingReturnForm: Form[CreateReturnForm] = Form {
    mapping(
      "status" -> nonEmptyText,
      "reason" -> nonEmptyText
    )(CreateReturnForm.apply)(CreateReturnForm.unapply)
  }

  def createReturn = Action.async { implicit request =>
    val returnOption = request.body.asJson.map(Json.fromJson[SimpleReturn](_))
    returnOption.map(returnJSVal => returnJSVal.fold(
      _ => Future(BadRequest),
      newReturn => returnRepository.create(newReturn.order, newReturn.reason).map(_ => Ok("Created")))
    ).getOrElse(Future(BadRequest))
  }

  def createReturnForm(order: Int) = Action { implicit request =>
    Ok(views.html.returns.createReturn(creatingReturnForm, order))
  }

  def createReturnHandle(order: Int) = Action.async { implicit request =>
    creatingReturnForm.bindFromRequest.fold(
      errorForm => Future.successful(Ok(views.html.returns.createReturn(errorForm, order))),
      ret => returnRepository.create(order, ret.reason, ret.status).map { _ =>
        Redirect(routes.ReturnController.viewReturns()).flashing("success" -> "return created")
      }
    )
  }

  def updateReturnForm(id: Int) = Action.async { implicit request =>
    val returnOption = returnRepository.getByIDOption(id)
    returnOption.map {
      ret =>
        ret.map(value => updatingReturnForm.fill(UpdateReturnForm(value.id, value.status, value.reason)))
          .map(form => Ok(views.html.returns.updateReturn(form)))
          .getOrElse(Ok("update return failed"))
    }
  }

  def updateReturnHandle = Action.async { implicit request =>
    updatingReturnForm.bindFromRequest.fold(
      errors => Future.successful(Ok(views.html.returns.updateReturn(errors))),
      returnObject => {
        returnRepository.update(returnObject.order, Return(returnObject.order, returnObject.status, returnObject.reason)).map { _ =>
          Redirect(routes.ReturnController.viewReturns()).flashing("success" -> "return updated")
        }
      }
    )
  }

  def deleteReturn(id: Int) = Action.async {
    returnRepository.delete(id).map(_ => Redirect(routes.ReturnController.viewReturns()).flashing("success" -> "return deleted"))
  }

  def listReturns = Action.async {
    returnRepository.list()
      .map(list => Json.toJson(list))
      .map(jsVal => Ok(jsVal))
  }

  def getReturn(id: Int) = Action.async {
    returnRepository.getByIDOption(id) map {
      case Some(payment) => Ok(Json.toJson(payment))
      case None => NotFound("Return not found")
    }
  }

  def viewReturns() = Action.async { implicit request =>
    returnRepository.list()
      .map(list => Ok(views.html.returns.returnsView(list)))
  }
}

case class UpdateReturnForm(order: Int, status: String, reason: String)

case class CreateReturnForm(status: String, reason: String)
