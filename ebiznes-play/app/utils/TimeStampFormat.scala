package utils

import java.sql.Timestamp

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}

object TimeStampFormat extends Format[Timestamp] {

  def reads(json: JsValue) = {
    val str = json.as[String]
    JsSuccess(TimeUtils.parseDate(str))
  }

  def writes(ts: Timestamp) = JsString(TimeUtils.formatDate(ts))
}