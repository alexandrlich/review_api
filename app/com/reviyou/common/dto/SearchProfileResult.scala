package com.reviyou.common.dto

import java.util.Date
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray


case class SearchProfileResult ( obj: JsObject ) {
//  id: String,
//  first_name: String,
//  last_name:String,
//  last_company: String
  def forRestResponse: JsObject = {
    val result = Json.obj(
      "id" -> obj.value.get("_id").get.as[String],
      "first_name"-> obj.value.get("first_name"),
      "last_name"-> obj.value.get("last_name")
    )
    if (!obj.value.get("jobs").isEmpty) {

      //old code: val jobs:JsArray = Json.arr(obj.value.get("jobs") map { p => p}).as[JsArray].value(0).as[JsArray]
      val jobsArray = obj.value.get("jobs").get.as[JsArray]
      //old code: val last_company = if(jobs != null) jobs.value(jobs.value.length-1) else null
      //TODO: should use the latest start_date and\or end_data if present rather than just last element in the list probably?
      val last_company: JsValue = jobsArray.value.last.as[JsValue] \ "company"
      //old code: result + ("last_company"-> last_company)
      return result.as[JsObject] ++ Json.obj("last_company"-> last_company)
    }
  result
}

}


