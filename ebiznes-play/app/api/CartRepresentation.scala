package api

import models.{CartProduct, Discount, Product}
import play.api.libs.json.Json

case class CartRepresentation(uuid: String, cartProducts: Seq[CartProductWithDiscount])

case class CartProductWithDiscount(product: ProductWithDiscount, amount: Int)


object CartProductWithDiscount {
  implicit val cartProductWithDiscountFormat = Json.format[CartProductWithDiscount]

  def apply(cp: (CartProduct, Product), d: Option[Discount]) = new CartProductWithDiscount(ProductWithDiscount.apply(cp._2, d), cp._1.amount)

}

object CartRepresentation {
  implicit val cartRepresentationFormat = Json.format[CartRepresentation]
}
