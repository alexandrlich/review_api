package com.reviyou.controllers

import com.reviyou.controllers.actions.AuthenticatedAction
import com.reviyou.services.UserService
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scala.concurrent.ExecutionContext




//Implicit

import ExecutionContext.Implicits.global


object UserController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)




  //move to UserSettings
  def saveSettingOption(user_id:String)  = AuthenticatedAction.async(parse.json)  {
    request =>
      val cObj = request.body.as[JsObject].value

      val name = cObj.get("settingName").get.as[String]
      val value = cObj.get("settingValue").get.as[Boolean]


      log.trace(s"saveSoundSettingOption, setting name: $name, boolean value: $value")


      UserService.updateSoundSettings(user_id,name, value).map(res => response(res))



  }

}