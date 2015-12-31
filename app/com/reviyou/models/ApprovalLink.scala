package com.reviyou.models

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

case class ApprovalLink(
                  override var _id: Option[BSONObjectID],
                  profileId: String,
                  //create_time: DateTime = ,
                  override var updated: Option[DateTime] = None,
                  override var created: Option[DateTime] = None
                  ) extends TemporalModel {
}

object ApprovalLink {

  import play.modules.reactivemongo.json.BSONFormats._
  import MongoJsFormat._
  // For MongoDB serialization

  // For MongoDB serialization
  implicit val approvalLinkFormat = Json.format[ApprovalLink]

}

