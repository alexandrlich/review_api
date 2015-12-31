package com.reviyou.controllers

import com.reviyou.controllers.actions.AuthenticatedAction
import com.reviyou.models.dto.CommentDto
import com.reviyou.services.CommentService
import com.reviyou.models.{UserModel, Comment}
import org.slf4j.LoggerFactory
import play.api.libs.json._
import com.reviyou.services.dao.{UserDao, CommentDao}
import scala.concurrent.{Promise, Future, Await, ExecutionContext}
import org.joda.time.DateTime
import scala.concurrent.duration.Duration
import com.reviyou.common.RestStatusCodes
import com.reviyou.common.Utils
import com.reviyou.services.db.DBQueryBuilder

import scala.util.{Left, Right}

//Implicit

import ExecutionContext.Implicits.global

/**
 * Created by zhgirov on 27.04.14.
 */
object CommentController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)


  //return new saved comment_id
  def save(profile_id: String) = AuthenticatedAction.async(parse.json) {
    request =>
      log.trace("save: profile_id: $profile_id");
      val cObj = request.body.as[JsObject].value
      val user_id = cObj.get("user_id").get.as[String]

      val commentText = cObj.get("text").get.as[String];

      Utils.validInput(commentText) match {
        //validation
        case false => errorAsyncResponse(RestStatusCodes.ERROR_VALID_INPUT_LIMIT, s"Size of the comment is not supported.")
        case true => CommentService.addComments(profile_id, user_id, commentText).map(res => response(res))
      }
  }


  /**
   * new logic - send 1 request to get N image url's from db per REST call
   * @param profile_id
   * @param user_id
   * @param user_token
   * @param offset
   * @return
   */
  def getNew(profile_id: String, user_id: String, user_token: String, offset: Int) = AuthenticatedAction.async {
    log.trace(s"getNew, profileId: $profile_id, user_id: $user_id, offset: $offset")

    CommentService.findComments(profile_id, user_id, offset) map {
      result => successResponse(
        JsArray(result.map(comment => Json.toJson(comment)))
      )
    }
  }

  /**
   * if it's user's comment - he can delete it
   * @param commentId
   * @return
   */
  def delete(commentId: String, userId: String) = AuthenticatedAction.async { request =>
    log.trace(s"delete commentId: $commentId, userId: $userId")
    CommentService.removeComment(commentId, userId).map(res => response(res))

    /*
    map {
      case Right(r) => successResponse(Json.obj())
      case Left(left) => errorResponse(RestStatusCodes.ERROR_DELETE_OBJ, left.message)
    }*/
  }



  def vote() = AuthenticatedAction.async(parse.json) { request =>
    val cObj = request.body.as[JsObject].value
    val userId = cObj.get("user_id").get.as[String]
    val commentId = cObj.get("comment_id").get.as[String]

    val groupName = cObj.get("group_name").get.as[String];


    log.trace(s"vote for  commentId: $commentId, userId: $userId, groupName: $groupName")
    CommentService.commentVote(commentId, userId, groupName, true) map {
      result => successResponse(result)
    }
  }

  def unvote() = AuthenticatedAction.async(parse.json)  { request =>
    val cObj = request.body.as[JsObject].value
    val userId = cObj.get("user_id").get.as[String]

    val commentId = cObj.get("comment_id").get.as[String]
    val groupName = cObj.get("group_name").get.as[String];

    log.trace(s"unvote for  commentId: $commentId, userId: $userId, groupName: $groupName")
    CommentService.commentVote(commentId, userId, groupName, false) map {
      result => successResponse(result)
    }
  }
}
