package com.reviyou.controllers

import com.reviyou.common.RestStatusCodes._
import com.reviyou.common.Utils._
import com.reviyou.controllers.actions.AuthenticatedAction
import com.reviyou.services.SkillService

import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import com.reviyou.models._
import com.reviyou.services.dao.{SkillDao}


/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import scala.language.postfixOps


object SkillController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)


  def getSkills(tags:String) = AuthenticatedAction.async {
    log.trace("getSkills: $tags")

    //move to utils
    val tagsList = ProfileController.convertToList(tags)
    //todo:get only tag specific skills

    SkillService.getSkills(tagsList) map {
      list =>
        successResponse(JsArray(list))
    }
  }


}
