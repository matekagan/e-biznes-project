package utils

import java.sql.Timestamp
import java.text.SimpleDateFormat

object TimeUtils {
  val timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")

  def formatDate(timestamp: Timestamp): String = timeFormat.format(timestamp)

  def parseDate(string: String): Timestamp = new Timestamp(timeFormat.parse(string).getTime)
}
