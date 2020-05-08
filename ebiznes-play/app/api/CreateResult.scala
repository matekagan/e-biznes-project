package api

import play.api.libs.json.Json

case class CreateResult(status: String, id: Option[String])

object CreateResult {
  def apply(uuid: String): CreateResult = new CreateResult("CREATED", Option(uuid))
  def apply(): CreateResult = new CreateResult("FAILED", Option.empty)
  implicit val resultFormat = Json.format[CreateResult]
}