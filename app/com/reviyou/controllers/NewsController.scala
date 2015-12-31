package com.reviyou.controllers

import com.reviyou.common.RestStatusCodes._
import com.reviyou.common.Utils._
import com.reviyou.controllers.actions.{AuthenticatedAction}
import com.reviyou.services.NewsService

import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import com.reviyou.models._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action

// Reactive Mongo plugin, including the JSON-specialized collection

import com.reviyou.models.Profile._

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import scala.language.postfixOps


object NewsController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)



  def getNews() = AuthenticatedAction.async {
    log.trace("getNews")
    NewsService.getNews() map {
      list =>
        successResponse(JsArray(list))
    }
  }


  def checkRecentNews= AuthenticatedAction.async {
    log.trace("checkRecentNews")
    NewsService.checkRecentNews() map {recent=>

      successResponse(recent)
    }
  }

}
