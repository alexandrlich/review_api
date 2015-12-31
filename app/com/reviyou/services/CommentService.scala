package com.reviyou.services


import com.reviyou.models.{UserModel, Comment}
import com.reviyou.models.dto.{CommentDto}
import com.reviyou.services.exceptions.{CustomServiceException, UnexpectedServiceException, ServiceException}


import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Json}
import com.reviyou.services.dao.{ProfileDao, CommentDao}
import com.reviyou.services.db.DBQueryBuilder

import scala.concurrent.{Promise, Future, ExecutionContext}
import play.api._
import play.api.Play.current

//Implicit
import com.reviyou.models.Comment._
import com.reviyou.common.RestStatusCodes._
import ExecutionContext.Implicits.global

object CommentService {

  val log = LoggerFactory.getLogger(getClass)

  //val log = play.Logger.of(CommentService.class);
  val defaultImagesPath = Play.application.configuration.getString("web.apiimage.user").get
  val COMMENTS_FETCH_SIZE = Play.application.configuration.getInt("comments.fetch.size").getOrElse(10)



  /**
   * add new comment, recalculate total amount of comments and update for profile,
   * increase popular index
   * @param profile_id
   * @param user_id
   * @param commentText
   * @return
   */
  def addComments(profile_id: String, user_id: String, commentText: String): Future[JsObject] = {
    log.trace(s"addComments : $profile_id user_id:  $user_id")

    com.reviyou.services.dao.UserDao.findById(user_id).flatMap {
      case Some(user) => insert(profile_id,user,commentText) flatMap {
        case Left(left) => Future(BaseServiceO.error(ERROR_INSERT_OBJ_TO_DB, left.message))
        case Right(right)=> {

          CommentDao.countByProfile(profile_id) map {commentsAmount=>
            ProfileDao.updatePopularAndCommentCounts(profile_id,1,commentsAmount)

          }

          val commentDto = convertCommentToDto(right, user.getUserId)
          Future(BaseServiceO.success(Json.toJson(commentDto).as[JsObject]))
        }
      }
      case _ => Future(BaseServiceO.error(ERROR_USR_NOT_FOUND, "User with id = $user_id has not been found."))
    }
  }



  def insert(profile_id: String, user: UserModel, commentText: String)= {
    log.trace("insert: ")
    val comment = Comment(
      None,//pk
      profile_id,
      user.getUserId,
      user.first_name,
      user.last_name,
      commentText,
      DateTime.now().getMillis)

    CommentDao.insert(comment)


  }

  /**
   * find comments with 2 requests to db
   * @param profileId
   * @param userId
   * @param offset
   * @return
   */
  def findComments(profileId: String, userId: String, offset: Int) = {
    log.trace(s"findCommentsNew : $profileId user_id:  $userId")
   for {
      comments <- CommentDao.findComments(profileId, offset, COMMENTS_FETCH_SIZE)
    } yield combine(comments, userId)

  }


  def combine(comments:List[Comment], userId: String): List[CommentDto] = {
    comments.map {comment=>
      convertCommentToDto(comment, userId)

    }
  }





  def convertCommentToDto(comment: Comment, userId: String) : CommentDto = {
    log.trace(s"userId:  $userId defaultImagesPath: $defaultImagesPath")
	  //"pic" -special extension to differenciate a spacial default images in nginx config

    val warmList = comment.group_warm.getOrElse(List[String]())
    val coldList = comment.group_cold.getOrElse(List[String]())
    val trollList = comment.group_troll.getOrElse(List[String]())
    val reportList = comment.group_report.getOrElse(List[String]())

    CommentDto(
      comment._id.map(_.stringify),
      comment.profile_id,
      comment.user_id,
      comment.user_first_name,
      comment.user_last_name,
      comment.text,
      comment.create_time,
      defaultImagesPath + comment.user_id + ".png",
      warmList.size,
      warmList.contains(userId),
      coldList.size,
      coldList.contains(userId),
      trollList.size,
      trollList.contains(userId),
      reportList.size,
      reportList.contains(userId)
      //,
      //consolidatedVotesInfo(comment.comment_votes, userId)
    )
  }


  /**
   * remove comment, recalculate remaining acmount and update for profile
   * decrease popular index
   *
   * @param commentId
   * @param userId
   * @return
   */
  def removeComment(commentId:String, userId:String): Future[JsObject] =  {
    log.trace(s"removeComment: $commentId, userId: $userId")

  //find record to get profileId
  CommentDao.findOne(DBQueryBuilder.and(DBQueryBuilder.id(commentId),
      Json.obj("user_id" -> userId))).flatMap {
    case Some(comment) => {
        CommentDao.remove(
          DBQueryBuilder.and(DBQueryBuilder.id(commentId), Json.obj("user_id" -> userId)), firstMatchOnly=true
        ) flatMap {
          case Left(left) => Future(BaseServiceO.error(ERROR_DELETE_OBJ, left.message))
          case Right(right)=> {


            CommentDao.countByProfile(comment.profile_id) map {commentsAmount=>
              ProfileDao.updatePopularAndCommentCounts(comment.profile_id,-1,commentsAmount)

            }

            Future(BaseServiceO.success(Json.obj()))
          }
        }
    }
    case _ => {
      Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND,"No associated comment found"))
    }
  }







  }

  /**
   * Vote or unvote for comment(one of 4 groups) you can vote for all 4.
   * @param commentId
   * @param userId
   * @param voteType is one of the following: warm_group, cold_group, troll_group, report_group
   * @param voteFor if true - vote for comment, otherwise unvote
   * @return
   */

  def commentVote(commentId: String, userId: String, voteType: String, voteFor: Boolean):Future[JsObject] = {
    //CommentDao.push(commentId, voteType, userId)
    val result = voteFor match {
      case true =>  CommentDao.update(commentId,DBQueryBuilder.addToSet(voteType, userId))
      case false =>CommentDao.update(commentId,DBQueryBuilder.pull(voteType, userId))
    }

    result flatMap {
      case Left(left) => Future(BaseServiceO.error(ERROR_COMMENT_VOTING, left.message))
      case Right(right)=> {
        Future(BaseServiceO.success())
      }

    }
  }


  def optionCommentDto(optComment: Option[Comment], userId: String): JsObject = {
    optComment.map(commentDtoJsonResult(_, userId)).getOrElse(BaseServiceO.error(ERROR_UPDATE_OBJ, "Unexpected error."))
  }

  def commentDtoJsonResult(comment: Comment, userId: String): JsObject = {
    val commentDto = convertCommentToDto(comment, userId)
    BaseServiceO.success(Json.toJson(commentDto).as[JsObject])
  }


}
