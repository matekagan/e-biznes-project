package models

import java.sql.Timestamp

import play.api.libs.json.{Format, Json, OFormat}
import utils.TimeStampFormat

case class Order(id: Int, createDate: Timestamp, address: String, value: Int, status: String, phone: String, userID: Int)

case class OrderProduct(orderID: Int, productID: Int, amount: Int)

object Order {
  implicit val timeStampFormat: Format[Timestamp] = TimeStampFormat

  implicit val paymentFormat: OFormat[Order] = Json.format[Order]
}
