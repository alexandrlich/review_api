package com.reviyou.models

import play.api.libs.json.{Json, JsObject}

//id: String,
//first_name: String,
//last_name: String,
//email: String,
//general_average_rank: Int,
//current_user_general_vote: Int,
//is_author: Boolean,
//jobs: List[Job],
//skills: List[Skill]
/*
case class ViewProfileResult (obj: JsObject ) {

  def forResponse: JsObject = {

    Json.obj(
      "id" -> obj.value.get("_id").get.as[JsObject].value.get("$oid"),
      "first_name"-> obj.value.get("first_name"),
      "last_name"-> obj.value.get("last_name"),
      "email" -> obj.value.get("email"),
      "general_average_rank" -> obj.value.get("general_average_rank"),
      "current_user_general_vote" -> obj.value.get("current_user_general_vote"),
      "is_author" -> false,
      "jobs" -> obj.value.get("jobs"),
      "skills" -> obj.value.get("skills")
    )
  }
}*/