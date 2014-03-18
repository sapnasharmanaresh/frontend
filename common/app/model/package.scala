package model

import com.gu.openplatform.contentapi.model.{ Content => ApiContent, MediaAsset }
import org.joda.time.format.ISODateTimeFormat
import scala.math.abs

object `package` {



  implicit class Media2rich(a: MediaAsset) {
    lazy val safeFields = a.fields.getOrElse(Map.empty)
  }

  implicit class Any2In[A](a: A) {
    def in(as: Set[A]): Boolean = as contains a
  }

  implicit class Int2RichInt(i: Int) {
    def distanceFrom(j: Int) = abs(j - i)
    def in(range: Range): Boolean = range contains i
  }

  lazy val DateTimeWithMillis = """.*\d\d:\d\d\.(\d+)[Z|\+].*""".r

  implicit class ISODateTimeStringNoMillis2DateTime(s: String) {
    lazy val parseISODateTime = s match {
      case DateTimeWithMillis(_) => ISODateTimeFormat.dateTime.parseDateTime(s)
      case _ => ISODateTimeFormat.dateTimeNoMillis.parseDateTime(s)
    }
  }
}
