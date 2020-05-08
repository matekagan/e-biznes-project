package models

import play.api.libs.json.Json

case class Advertisement(id: Int, text: String, link: String)

object Advertisement {
  implicit val adFormat = Json.format[Advertisement]
}
