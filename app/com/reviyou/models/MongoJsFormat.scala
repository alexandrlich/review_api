package com.reviyou.models

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
/**
 * Created by Alex on 2/1/15.
 */
object MongoJsFormat {
  private val dateFmt = ISODateTimeFormat.dateTime()

  implicit val dateTimeRead: Reads[DateTime] = (
    (__ \ "$date").read[Long].map { dateTime =>
      new DateTime(dateTime)
    }
    )

  implicit val dateTimeWrite: Writes[DateTime] = new Writes[DateTime] {
    def writes(dateTime: DateTime): JsValue = Json.obj(
      "$date" -> dateTime.getMillis
    )
  }
}
