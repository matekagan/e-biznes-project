package api

import java.sql.Timestamp

import play.api.libs.json.{Format, Json, OFormat}
import utils.TimeStampFormat
import models.{OrderProduct, Product}

case class SimpleOrderRepresentation(homeDelivery: Boolean, address: String, orderProducts: Seq[ProductWithAmount], firstName: String, lastName: String, email: String, phone: String) {
  def getOrderProducts(id: Int): Seq[OrderProduct] = orderProducts.map(_.getOrderProduct(id))
}
case class ProductWithAmount(amount: Int, productID: Int) {
  def getOrderProduct(orderID: Int) = OrderProduct(orderID, productID, amount)
}

object SimpleOrderRepresentation {
  implicit val timeStampFormat: Format[Timestamp] = TimeStampFormat

  implicit val orderRepresentationFormat: OFormat[SimpleOrderRepresentation] = Json.format[SimpleOrderRepresentation]
  implicit val productFormat: OFormat[Product] = Json.format[Product]
}

object ProductWithAmount {
  implicit val orderProductFormat: OFormat[ProductWithAmount] = Json.format[ProductWithAmount]
}
