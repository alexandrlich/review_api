package com.reviyou.models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
 * Object for storing votes resulting data into DB
 *
 * Created by zhgirov on 11.05.14.
 */
case class Vote(
                 vote_value: Int,
                 vote_time: Long,
                 profile_id: String,
                 //vote_type: String,
                 skill_id: Option[String],
                 user_id: String,
                 show: Option[Boolean],//show on votedProfiles page
                 override var updated: Option[DateTime] = None,
                 override var created: Option[DateTime] = None
                 ) extends TemporalModel {

  override var _id: Option[BSONObjectID] = None
}

object Vote {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val voteFormat = Json.format[Vote]
}
