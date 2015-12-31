package com.reviyou.models.dto

import play.api.libs.json.Json

/**
 * Created by eugenezhgirov on 8/16/14.
 */
case class AggregateReturnedResult(_id: String, votesCount: Int, votesSum: Double)


object AggregateReturnedResult {

  implicit val aggregateResultFormat = Json.format[AggregateReturnedResult]

}