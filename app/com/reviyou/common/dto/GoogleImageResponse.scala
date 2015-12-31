package com.reviyou.common.dto

import play.api.libs.json.Json


case class GoogleImageResponse(image:String)

object GoogleImageResponse  {

  implicit val googleImageResponseFormat = Json.format[GoogleImageResponse]
}