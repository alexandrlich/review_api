package com.reviyou.services

import com.reviyou.common.RestStatusCodes._
import com.reviyou.models.Bookmark
import com.reviyou.services.ProfileService._
import com.reviyou.services.exceptions.ServiceException
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Json}
import reactivemongo.bson.BSONObjectID
import com.reviyou.services.dao.{VoteDao, ProfileDao, BookmarkDao}
import com.reviyou.services.db.DBQueryBuilder

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by eugenezhgirov on 6/22/14.
 */
object BookmarkService {

  val log = LoggerFactory.getLogger(getClass)

  def add(profileId: String, userId: String):Future[JsObject] = {
    BookmarkDao.findOne(DBQueryBuilder.query(
        DBQueryBuilder.and(Json.obj("profileId" -> profileId), Json.obj("userId" -> userId))
      )
    ) flatMap {
      case Some(b) => Future(BaseServiceO.success())
      case _ => insert(Bookmark(None, profileId, userId))
    }
  }

  def insert(bookmark: Bookmark): Future[JsObject] = {
    BookmarkDao.insert(bookmark) map {
      case Left(left) => BaseServiceO.error(ERROR_INSERT_OBJ_TO_DB, left.message)
      case Right(right) =>  BaseServiceO.success(Json.toJson[Bookmark](right).as[JsObject])
    }
  }

  //TODO: add projections
  def getBookmarks(userId: String, offset: Int) = {
    BookmarkDao.find(DBQueryBuilder.query(Json.obj("user_id" -> userId)), startFrom = offset).map {
      bookmarks =>
        val profileIds = bookmarks.map(bookmark => BSONObjectID(bookmark.profile_id))

        ProfileService.getProfilesByIds(profileIds)

      }
  }

  def getFollowerIds(profileId: String):Future[List[String]] = {
    BookmarkDao.findFollowers(profileId).map {followers =>
      log.trace("found amount of followers: " + followers.size);
      followers.map(follower => follower.user_id)
    }
  }

  def remove(profileId: String, userId: String):Future[Either[ServiceException, Boolean]] = {
    log.trace(s"remove, profileId: $profileId, userId: $userId")
    BookmarkDao.remove(
      DBQueryBuilder.and(Json.obj("profile_id" -> profileId), Json.obj("user_id" -> userId)), firstMatchOnly=true
    )
  }

  def removeAll(profileId: String):Future[Either[ServiceException, Boolean]] = {
    log.trace(s"removeAll, profileId: $profileId")
    BookmarkDao.remove(
      DBQueryBuilder.and(Json.obj("profile_id" -> profileId)), firstMatchOnly=false
    )
  }
}
