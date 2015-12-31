package com.reviyou.services

import com.reviyou.common.RestStatusCodes
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, Json, JsObject}


trait BaseService {

  val baselog = LoggerFactory.getLogger(getClass)

  def success(obj:JsObject = Json.obj()):JsObject = {
    Json.obj("status" -> RestStatusCodes.OK, "data" -> obj)
  }

  def success(obj:JsArray):JsObject = {
    Json.obj("status" -> RestStatusCodes.OK, "data" -> obj)
  }

  def error(code:Int, errmessage:String):JsObject = {
    baselog.error(s"code: $code, message: $errmessage.")
    Json.obj("status" -> code, "error_message" -> errmessage)
  }

  def error(code:Int, errmessage:String, detailedMsg: String):JsObject = {
    baselog.error(s"code: $code, message: $errmessage, detailedMsg:$detailedMsg")
    Json.obj("status" -> code, "error_message" -> errmessage, "error_info" -> detailedMsg)
  }



}

object BaseServiceO extends BaseService