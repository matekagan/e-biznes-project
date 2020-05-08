package models

import java.sql.Timestamp

import play.api.libs.json._
import utils.TimeStampFormat


case class Payment(id: Int, order: Int, value: Int, createdTime: Timestamp, status: String)

object Payment {
  implicit val timeStampFormat: Format[Timestamp] = TimeStampFormat

  implicit val paymentFormat = Json.format[Payment]

}

