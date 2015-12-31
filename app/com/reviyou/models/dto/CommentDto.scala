package com.reviyou.models.dto

import play.api.libs.json.Json

/**
 *
 * Created by eugenezhgirov on 9/25/14.
 */
case class CommentDto (
                    comment_id: Option[String],
                    profile_id: String,
                    user_id: String,
                    user_first_name: String,
                    user_last_name: String,
                    text: String,
                    create_time: Long,
                    user_image_url: String,
                    warm_count: Long,
                    warm_voted: Boolean,
                    cold_count:Long,
                    cold_voted:Boolean,
                    troll_count:Long,
                    troll_voted:Boolean,
                    report_count:Long,
                    report_voted:Boolean

//comment_votes: Option[Map[String, VoteGroup]] = None
                    )

//case class VoteGroup (votes_count: Int, is_voted: Boolean)

object CommentDto {

  // For MongoDB serialization
  //implicit val voteGroupFormat = Json.format[VoteGroup]
  implicit val commentDtoFormat = Json.format[CommentDto]
}

