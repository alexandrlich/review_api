package com.reviyou.controllers

import com.reviyou.controllers.actions.AuthenticatedAction
import com.reviyou.models.dto.CommentDto
import com.reviyou.services.{TagsService, UserService, CommentService}
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


object TagController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)


  //search by first letters
  def searchTags(query: String) = AuthenticatedAction.async {
    log.trace("searchTagsCtrl");


    TagsService.searchTags(query) map (
      res=>
        successResponse(JsArray(res)))


  }

  /**
   *
   * @param offset-  deprecated
   * @return
   */
  def get(offset: Int) = AuthenticatedAction.async {

      TagsService.findTags(offset) map (
        res=>
      successResponse(JsArray(res)))

  }

  //move to UserController?
  def saveSelected(user_id:String)  = AuthenticatedAction.async(parse.json)  {
    request =>
      val cObj = request.body.as[JsObject].value

      val tag_name = cObj.get("tag_name").get.as[String];
      log.trace(s"saveSelected, tag_name: $tag_name");

      tag_name match {
        case "" => errorAsyncResponse(RestStatusCodes.ERROR_OBJ_NOT_FOUND,"missing tag name")
        case _ =>UserService.addRemoveTag(user_id, tag_name,true).map(res => response(res))
      }

  }

  /**
   *
   * @deprecated
   * from old 1.2.2 api, use deleteSelected instead
   * @param user_id
   * @return
   */
  def deleteSelectedDeprecated(user_id:String)  = AuthenticatedAction.async(parse.json)  {
    request =>
      val cObj = request.body.as[JsObject].value
      val tag_name = cObj.get("tag_name").get.as[String];
      log.trace(s"deleteSelected, tag_name: $tag_name");

      tag_name match {
        case "" => errorAsyncResponse(RestStatusCodes.ERROR_OBJ_NOT_FOUND,"missing tag name")
        case _ =>UserService.addRemoveTag(user_id, tag_name,false).map(res => response(res))
      }

  }

  /**
   * tag_name could be 1 tag,
   * tag_names could be multiple tags
   * @param user_id
   * @return
   */
  def deleteSelected(user_id:String)  = AuthenticatedAction.async(parse.json)  {
    request =>
      val cObj = request.body.as[JsObject].value

      val tag_names = cObj.get("tag_names").get.as[String];

      val list = ProfileController.convertToList(tag_names)

      log.debug("size:" + list.size)


      list.size match {
        case 0 => errorAsyncResponse(RestStatusCodes.ERROR_OBJ_NOT_FOUND,"missing tag name")
        case 1 =>UserService.addRemoveTag(user_id, list(0),false).map(res => response(res))
        case _ => {
          //todo: write 1 query to pull 10 tags at once
          list.map {tag=>
            UserService.addRemoveTag(user_id, tag,false) 
          }
          Future.successful(successResponse(Json.obj()))
        }
      }

  }
}