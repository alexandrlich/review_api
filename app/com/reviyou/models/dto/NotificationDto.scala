package com.reviyou.models.dto

import play.api.libs.json.Json

case class NotificationDto (
                    id: String,
                    profile_id: String,
                    //profile_name: String,
                    text: String,
                    follower_id: String,
                    //profilesCommentedCount: Long,
                    create_time: Long,
                    ack : Boolean
                    )

//case class VoteGroup (votes_count: Int, is_voted: Boolean)

object NotificationDto {

  // For MongoDB serialization
  implicit val notificationDtoFormat = Json.format[NotificationDto]
}

