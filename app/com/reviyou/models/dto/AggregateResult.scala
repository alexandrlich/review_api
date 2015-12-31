package com.reviyou.models.dto

import play.api.libs.json.Json

/**
 * Created by eugenezhgirov on 8/5/14.
 */
case class AggregateResult(_id: String = "",
                                  iVotesCount: Int = 0,
                                  iVotesSum: Double = 0d,
                                  votesCount: Int = 0,
                                  votesSum: Double = 0d)

object AggregateResult {

  implicit val aggregateProfileResultFormat = Json.format[AggregateResult]

}

