package controllers.shop

import api.{CartProductWithDiscount, CreateResult, ProductWithAmount}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repositories.CartRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartController @Inject()(cartRepository: CartRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def createCart = Action.async { request =>
    val cartProducts = request.body.asJson.map(Json.fromJson[Seq[ProductWithAmount]](_))
    cartProducts.map(jsVal => jsVal.fold(
      _ => Future(BadRequest(Json.toJson(CreateResult()))),
      products => cartRepository.create(products).map(uuid => Ok(Json.toJson(CreateResult(uuid)))))
    ).getOrElse(Future(BadRequest(Json.toJson(CreateResult()))))
  }

  def handleUpdate(id: Int) = Action {
    Ok("updated cart") //TODO: handle
  }

  def getByID(id: Int) = Action.async {
    cartRepository.getByIDOption(id) map {
      case Some(cart) => Ok(Json.toJson(cart))
      case None => NotFound("Cart not found")
    }
  }

  def listCarts = Action.async {
    cartRepository.list()
      .map(list => Json.toJson(list))
      .map(json => Ok(json))
  }
  def deleteCart(uuid: String) = Action.async {
    cartRepository.delete(uuid).map(_ => Ok("deleted"))
  }

  def deleteCartByID(id: Int) = Action.async {
    cartRepository.delete(id).map(_ => Redirect(routes.CartController.viewCarts()).flashing("success" -> "cart deleted"))
  }

  def getProducts(uuid: String) = Action.async {
    cartRepository.getProductsWithAmount(uuid)
      .map(_.map(v => CartProductWithDiscount(v._1, v._2)))
      .map(list => Ok(Json.toJson(list)))
  }

  def viewCarts() = Action.async { implicit request =>
    cartRepository.list()
      .map(list => Ok(views.html.carts.cartsView(list)))
  }
}
