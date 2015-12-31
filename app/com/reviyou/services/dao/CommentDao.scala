package com.reviyou.services.dao

import com.reviyou.models.Comment
import com.reviyou.services.db.DBQueryBuilder
import org.joda.time.DateTime
import reactivemongo.bson.{BSONInteger, BSONValue, BSONDocument, BSONArray}
import reactivemongo.core.commands._
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending
import play.api.libs.json.{JsObject, JsValue, Json}
import play.modules.reactivemongo.json.BSONFormats._

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._

/**
 * Created by zhgirov on 27.04.14.
 */
object CommentDao extends DocumentDAO[Comment] {

  val collectionName = "comments"

  def countByProfile(profileId: String): Future[Int] = {
    log.debug(s"countByProfile[profileId=$profileId]")

    val query = BSONDocument("profile_id" -> profileId)
    val command = Count(collectionName, Some(query))

    db.command(command) // returns Future[Int]
  }





  def otherPeopleComments(profileId: String, userId: String):Future[Boolean] = {
    //val query = BSONDocument("profile_id" -> profileId)

    val query =  DBQueryBuilder.and(Json.obj("profile_id" -> profileId), Json.obj("user_id" -> Json.obj("$ne" -> userId)))

    super.findOne(query).map {
        case Some(res) =>true
        case None =>false
    }

  }

  //get list of comments
  def findComments(profile_id: String, offset: Int, fetch_size:Int): Future[List[Comment]] = {
    CommentDao.find(DBQueryBuilder.query(Json.obj("profile_id" -> profile_id))
      ++ DBQueryBuilder.orderBy(Json.obj("create_time" -> -1)), startFrom = offset, upTo = fetch_size )

  }

  def findRecentComments(sinceTime: DateTime):Future[List[Comment]] = {

    //sort by date, only return profileId, userId and create_date
    CommentDao.find(DBQueryBuilder.query(DBQueryBuilder.gt("create_time", sinceTime.getMillis))
        ++ DBQueryBuilder.orderBy(Json.obj(Comment.createTimeFieldName -> -1)),
      upTo = 5000)


  }

}
