package models

import java.sql.Timestamp

import play.api.libs.json.{Format, Json}
import utils.TimeStampFormat

case class Cart(id: Int, uuid: String, createdAt: Timestamp)
case class CartProduct(orderID: Int, productID: Int, amount: Int)

object Cart {
  implicit val timeStampFormat: Format[Timestamp] = TimeStampFormat
  implicit val cartFormat = Json.format[Cart]
}