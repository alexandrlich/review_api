package com.reviyou.services.dao

import scala.concurrent.{Await, Future}
import reactivemongo.api.indexes.IndexType.Ascending
import com.reviyou.models.Bookmark
import reactivemongo.bson.BSONObjectID
import com.reviyou.services.db.DBQueryBuilder
import play.api.libs.json.Json
import scala.concurrent.duration.Duration._

/**
 * Created by zhgirov on 24.05.14.
 */
object BookmarkDao extends DocumentDAO[Bookmark] {

  val collectionName = "bookmarks"

  implicit class profileIsBookmarked(val profileId: Option[BSONObjectID]) extends AnyVal {
    def isBookmarked = {

      val pId = profileId.map(_.stringify).getOrElse("")
      val fProfile = findOne(DBQueryBuilder.query(Json.obj("profile_id" -> pId)))

        val result = for {
          mProfile <- fProfile
          fResult <- Future(mProfile.exists(_ != null))
        } yield fResult

        Await.result(result, Inf)
    }
  };


  def findFollowers(profileId:String ) = {
    find(DBQueryBuilder.query(Json.obj("profile_id" -> profileId)))
  }



}
