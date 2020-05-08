package api

import models.{Discount, Product}
import play.api.libs.json.Json

case class ProductWithDiscount(id: Int, name: String, description: String, category: Int, price: Int, discount: Option[Discount])

object ProductWithDiscount {
  def apply(product: Product, discount: Option[Discount]): ProductWithDiscount = {
    new ProductWithDiscount(product.id, product.name, product.description, product.category, product.price, discount)
  }

  implicit val productFormat = Json.format[ProductWithDiscount]
}
