package utils

import com.mohiva.play.silhouette.api.util.Clock
import org.joda.time.{DateTime, DateTimeZone}

class ClockImpl extends Clock{
  override def now: DateTime = DateTime.now(DateTimeZone.getDefault)
}
