package api

import auth.model.User
import models.Opinion
import play.api.libs.json.{Json, OFormat}

case class OpinionTemplate(product: Int, rating: Int, comment: String)

case class OpinionWithAuthor(id: Int, product: Int, rating: Int, comment: String, author: String)

object OpinionTemplate {
  implicit val formatter: OFormat[OpinionTemplate] = Json.format[OpinionTemplate]
}

object OpinionWithAuthor {
  def apply(opinion: Opinion, user: User): OpinionWithAuthor =
    new OpinionWithAuthor(opinion.id, opinion.product, opinion.rating, opinion.comment, user.name.getOrElse("Anonymous"))

  implicit val formatter: OFormat[OpinionWithAuthor] = Json.format[OpinionWithAuthor]
}
