package com.reviyou.controllers

import com.reviyou.controllers.actions.AuthenticatedAction
import com.reviyou.services.BaseServiceO
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scala.concurrent.{Future, ExecutionContext}
import com.reviyou.models.{Bookmark, Feedback}
import com.reviyou.services.dao.FeedbackDao
import com.reviyou.common.RestStatusCodes._

//Implicit

import ExecutionContext.Implicits.global

/**
 * Created by zhgirov on 02.05.14.
 */
object FeedbackController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)

  def contactUs = AuthenticatedAction.async(parse.json) {
    request =>
      log.trace(s"contactUs");

      val feedback:Feedback = request.body.as[Feedback]

      FeedbackDao.insert(feedback) map {
        case Left(left) => errorResponse(ERROR_INSERT_OBJ_TO_DB, left.message)
        case Right(right) =>  successResponse(Json.toJson(right).as[JsObject])

      }
  }

}
