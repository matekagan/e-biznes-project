package controllers.shop

import javax.inject.{Inject, Singleton}
import models.Advertisement
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import repositories.AdvertisementRepository
import utils.RandomSelector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdvertisementController @Inject()(advertisementRepository: AdvertisementRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  def newAdvertisementForm: Form[CreateAdvertisementForm] = Form {
    mapping(
      "text" -> nonEmptyText,
      "link" -> nonEmptyText
    )(CreateAdvertisementForm.apply)(CreateAdvertisementForm.unapply)
  }

  def updatingAdvertisementForm: Form[UpdateAdvertisementForm] = Form {
    mapping(
      "id" -> number,
      "text" -> nonEmptyText,
      "link" -> nonEmptyText
    )(UpdateAdvertisementForm.apply)(UpdateAdvertisementForm.unapply)
  }

  def createAdForm = Action { implicit request =>
    Ok(views.html.adverts.advertisementCreate(newAdvertisementForm))
  }

  def createAd: Action[AnyContent] = Action.async { implicit request =>
    newAdvertisementForm.bindFromRequest.fold(
      errorForm => Future.successful(BadRequest(views.html.adverts.advertisementCreate(errorForm))),
      cat => advertisementRepository.create(cat.text, cat.link).map { _ =>
        Redirect(routes.AdvertisementController.viewAds()).flashing("success" -> "AD created")
      }
    )
  }

  def updateAdForm(id: Int): Action[AnyContent] = Action.async { implicit request =>
    val advertOption = advertisementRepository.getByIDOption(id)
    advertOption.map {
      advert =>
        advert.map(value => updatingAdvertisementForm.fill(UpdateAdvertisementForm(value.id, value.text, value.link)))
          .map(form => Ok(views.html.adverts.advertisemsentUpdate(form)))
          .getOrElse(Ok("Error ?!"))
    }
  }

  def updateAd = Action.async { implicit request =>
    updatingAdvertisementForm.bindFromRequest.fold(
      errors => Future.successful(Ok(views.html.adverts.advertisemsentUpdate(errors))),
      advert => {
        advertisementRepository.update(advert.id, Advertisement(advert.id, advert.text, advert.link)).map { _ =>
          Redirect(routes.AdvertisementController.viewAds()).flashing("success" -> "Advert updated")
        }
      }
    )
  }

  def deleteAd(id: Int) = Action.async { implicit request =>
    advertisementRepository.delete(id).map(_ => Redirect(routes.AdvertisementController.viewAds()))
  }

  def listAds: Action[AnyContent] = Action.async { implicit request =>
    advertisementRepository.list()
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def getAd(id: Int): Action[AnyContent] = Action.async {
    advertisementRepository.getByIDOption(id) map {
      case Some(category) => Ok(Json.toJson(category))
      case None => NotFound("Advertisement not found")
    }
  }

  def getRandomAds(amountOption: Option[Int]) = Action.async {
    val amount = amountOption.getOrElse(5)
    advertisementRepository.list()
      .map(list => RandomSelector.selectRandom(amount, list))
      .map(list => Ok(Json.toJson(list)))
  }

  def viewAds() = Action.async { implicit request =>
    advertisementRepository.list()
      .map(list => Ok(views.html.adverts.advertisementView(list)))
  }
}

case class CreateAdvertisementForm(text: String, link: String)

case class UpdateAdvertisementForm(id: Int, text: String, link: String)