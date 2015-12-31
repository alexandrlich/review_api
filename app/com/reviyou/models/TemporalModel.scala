package com.reviyou.models

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime

/**
 * Created by zhgirov on 23.04.14.
 */
trait TemporalModel extends IdentifiableModel {
  var created: Option[DateTime]
  var updated: Option[DateTime]

  implicit val jodaDateFormat: Format[DateTime] = new Format[DateTime] {
    override def writes(dt: DateTime): JsValue = JsString(dt.toString)

    override def reads(json: JsValue): JsResult[DateTime] = JsSuccess(DateTime.parse(json.as[String]))
  }
}

trait IdentifiableModel {
  var _id: Option[BSONObjectID]

  def identify = _id.map(value => value.stringify).getOrElse("")
}
