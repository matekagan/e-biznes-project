package models

import java.sql.Timestamp

import play.api.libs.json.{Format, Json, OFormat}
import utils.TimeStampFormat

case class Opinion(id: Int, product: Int, rating: Int, comment: String, timestamp: Timestamp)

object Opinion {
  implicit val timeStampFormat: Format[Timestamp] = TimeStampFormat

  implicit val optionFormatter: OFormat[Opinion] = Json.format[Opinion]
}
