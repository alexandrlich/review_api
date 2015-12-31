package com.reviyou.models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json


case class Tag(
                  override var _id: Option[BSONObjectID],
                  name: String,
                  subtags: Option[List[String]],
                  override var updated: Option[DateTime] = None,
                  override var created: Option[DateTime] = None
                  ) extends TemporalModel {
}

object Tag {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val tagFormat = Json.format[Tag]

}
