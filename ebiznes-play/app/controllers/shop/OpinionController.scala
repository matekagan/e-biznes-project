package controllers.shop

import api.{OpinionTemplate, OpinionWithAuthor}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import controllers.auth.{SilhouetteController, SilhouetteControllerComponents}
import javax.inject.{Inject, Singleton}
import models.Opinion
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import repositories.OpinionRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OpinionController @Inject()(opinionRepository: OpinionRepository, cc: SilhouetteControllerComponents)(implicit ec: ExecutionContext) extends SilhouetteController(cc) {

  def createOpinion = SecuredAction.async { implicit request: SecuredRequest[EnvType, AnyContent] =>
    val user = getAuthorizedUser(request)
    val newOpinionTemplate = request.body.asJson.map(Json.fromJson[OpinionTemplate](_))
    newOpinionTemplate.map(jsVal => jsVal.fold(
      _ => Future(BadRequest),
      opinion => user.flatMap(u =>
        opinionRepository.create(opinion.product, opinion.rating, opinion.comment, u.id).map(_ => NoContent))
    )
    ).getOrElse(Future(BadRequest))
  }

  def updateOpinion(id: Int) = SecuredAction.async { implicit request =>
    val newOpinion = request.body.asJson.map(Json.fromJson[Opinion](_))
    newOpinion.map(jsVal => jsVal.fold(
      _ => Future(BadRequest),
      opinion => opinionRepository.update(id, opinion)
        .map(_ => NoContent))
    ).getOrElse(Future(BadRequest))
  }

  def deleteOpinion(id: Int) = SecuredAction.async { implicit request =>
    opinionRepository.delete(id).map(_ => NoContent)
  }

  def removeOpinion(id: Int) = SecuredAction.async { implicit request =>
    opinionRepository.delete(id).map(_ => Redirect(routes.OpinionController.viewOpinions()).flashing("success" -> "opinion deleted"))
  }

  def listOpinions: Action[AnyContent] = Action.async { implicit request =>
    opinionRepository.list()
      .map(list => Json.toJson(list))
      .map(json => Ok(json))
  }

  def listProductOpinions(id: Int) = Action.async { implicit request =>

    opinionRepository.getByProductID(id)
      .map(res => res.map(t => OpinionWithAuthor(t._1, t._2)))
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