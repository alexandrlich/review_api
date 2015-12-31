package com.reviyou.models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
 * Created by zhgirov on 17.06.14.
 */
case class ProfileVisitors(override var _id: Option[BSONObjectID],
                           user_ids: List[String],
                           override var updated: Option[DateTime] = None,
                           override var created: Option[DateTime] = None) extends TemporalModel {

}

object ProfileVisitors {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val profileVisitorsFormat = Json.format[ProfileVisitors]
}
