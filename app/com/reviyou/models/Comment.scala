package com.reviyou.models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json

case class Comment(
                    override var _id: Option[BSONObjectID],
                    profile_id: String,
                    user_id: String,
                    user_first_name: String,
                    user_last_name: String,
                    text: String,
                    create_time: Long,
                    //comment_votes: Option[List[CommentVote]] = None
                    group_warm: Option[List[String]] = None,
                    group_cold: Option[List[String]] = None,
                    group_troll: Option[List[String]] = None,
                    group_report: Option[List[String]] = None
                    ) extends TemporalModel {

  override var updated: Option[DateTime] = None
  override var created: Option[DateTime] = None
}

//case class CommentVote(user_id: String,
  //                    vote_type: String)

object Comment {

  val createTimeFieldName = "create_time"

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  //implicit val commentVoteFormat = Json.format[CommentVote]
  implicit val commentFormat = Json.format[Comment]
}