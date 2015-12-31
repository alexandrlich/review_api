package com.reviyou.models

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

case class News(
                  override var _id: Option[BSONObjectID],
                  title: String,
                  content: String,
                  create_time: DateTime,
                  override var updated: Option[DateTime] = None,
                  override var created: Option[DateTime] = None
                  ) extends TemporalModel {
}

object News {

  import play.modules.reactivemongo.json.BSONFormats._
  import MongoJsFormat._
  // For MongoDB serialization
  implicit val newsFormat = Json.format[News]

}

