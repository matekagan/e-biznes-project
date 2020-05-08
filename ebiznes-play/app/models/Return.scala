package models

import play.api.libs.json.Json

case class Return (id: Int, status: String, reason: String)

object Return {
  implicit val productFormat = Json.format[Return]
}
