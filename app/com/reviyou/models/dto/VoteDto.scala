package com.reviyou.models.dto

import play.api.libs.json.Json

/**
 * Created by eugenezhgirov on 7/30/14.
 */
case class VoteDto(
                    general_average_rank: Option[Int] = None,
                    skill_average_rank: Option[Int] = None,
                    votes_count: Option[Int]
//                    general_vote: Option[Double] = None,
//                    vote_value: Option[Double] = None
                    )

object VoteDto {

  implicit val voteDtoFormat = Json.format[VoteDto]
}
