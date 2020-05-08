package controllers.shop

import api.OpinionTemplate
import javax.inject.{Inject, Singleton}
import models.Opinion
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import repositories.OpinionRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OpinionController @Inject()(opinionRepository: OpinionRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def createOpinion = Action.async { request =>
    val newOpinionTemplate = request.body.asJson.map(Json.fromJson[OpinionTemplate](_))
    newOpinionTemplate.map(jsVal => jsVal.fold(
      _ => Future(BadRequest),
      opinion => opinionRepository.create(opinion.product, opinion.rating, opinion.comment)
        .map(_ => NoContent))
    ).getOrElse(Future(BadRequest))
  }

  def updateOpinion(id: Int) = Action.async { implicit request =>
    val newOpinion = request.body.asJson.map(Json.fromJson[Opinion](_))
    newOpinion.map(jsVal => jsVal.fold(
      _ => Future(BadRequest),
      opinion => opinionRepository.update(id, opinion)
        .map(_ => NoContent))
    ).getOrElse(Future(BadRequest))
  }

  def deleteOpinion(id: Int) = Action.async { implicit request =>
    opinionRepository.delete(id).map(_ => NoContent)
  }

  def removeOpinion(id: Int) = Action.async { implicit request =>
    opinionRepository.delete(id).map(_ => Redirect(routes.OpinionController.viewOpinions()).flashing("sucess" -> "opinion deleted"))
  }

  def listOpinions: Action[AnyContent] = Action.async { implicit request =>
    opinionRepository.list()
      .map(list => Json.toJson(list))
      .map(json => Ok(json))
  }

  def listProductOpinions(id: Int) = Action.async { implicit request =>
    opinionRepository.getByProductID(id)
      .map(list => Json.toJson(list))
      .map(json => Ok(json))
  }

  def getOpinion(id: Int): Action[AnyContent] = Action.async { implicit request =>
    opinionRepository.getByIDOption(id) map {
      case Some(product) => Ok(Json.toJson(product))
      case None => NotFound("product not found")
    }
  }

  def viewOpinions() = Action.async { implicit request =>
    opinionRepository.list()
      .map(list => Ok(views.html.opinions.opinionsView(list)))
  }
}