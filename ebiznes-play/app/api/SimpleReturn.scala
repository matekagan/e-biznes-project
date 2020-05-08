package api

import play.api.libs.json.Json

case class SimpleReturn(order: Int, reason: String)

object SimpleReturn {
  implicit val returnsFormat = Json.format[SimpleReturn]
}
