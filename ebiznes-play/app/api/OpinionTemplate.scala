package api

import play.api.libs.json.{Json, OFormat}

case class OpinionTemplate(product: Int, rating: Int, comment: String)

object OpinionTemplate {
  implicit val formatter: OFormat[OpinionTemplate] = Json.format[OpinionTemplate]
}
