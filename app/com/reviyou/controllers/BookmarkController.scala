package com.reviyou.controllers

//import play.api.mvc._

import com.reviyou.common.RestStatusCodes
import com.reviyou.controllers.ProfileController._
import com.reviyou.controllers.actions.AuthenticatedAction
import org.slf4j.LoggerFactory
import play.api.libs.json.{Json, JsArray, JsObject}
import com.reviyou.services.{ProfileService, BookmarkService}
import com.reviyou.services.dao.BookmarkDao
import com.reviyou.models.Bookmark

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by zhgirov on 23.05.14.
 */
object BookmarkController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)

  def add(profileId: String) = AuthenticatedAction.async(parse.json) { request =>
    val userId = request.body.as[JsObject].value.get("user_id").get.as[String]

    BookmarkService.add(profileId, userId).map(res => response(res))
  }

  def getBookmarkedProfiles(userId: String, offset: Int) = AuthenticatedAction.async { request =>
    log.trace(s"getBookmarkedProfiles, userId: $userId ");

    BookmarkService.getBookmarks(userId, offset).flatMap { fList =>
      fList.map(list => successResponse(JsArray(list)))
    }
  }

  def remove(profileId: String, userId: String) = AuthenticatedAction.async { request =>
    BookmarkService.remove(profileId, userId) map {
        case Right(r) => successResponse(Json.obj())
        case Left(left) => errorResponse(RestStatusCodes.ERROR_DELETE_OBJ, left.message)
      }
  }
}
