package com.reviyou.controllers

import com.reviyou.common.RestStatusCodes._
import com.reviyou.common.Utils._
import com.reviyou.controllers.actions.AuthenticatedAction
import com.reviyou.services.{ProfessionService, SkillService}

import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import com.reviyou.models._
import com.reviyou.services.dao.{SkillDao}


/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import scala.language.postfixOps


object ProfessionController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)


  def getProfessions() = AuthenticatedAction.async {
    log.trace("getProfessions")

    ProfessionService.getCommonProfessions() map {
      list =>
        successResponse(JsArray(list))
    }
  }


}
