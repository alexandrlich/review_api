package com.reviyou.controllers.bookmark.remove

import com.reviyou.models.Bookmark
import reactivemongo.bson.BSONObjectID
import play.api.test.Helpers._
import com.reviyou.services.dao.BookmarkDao
import com.reviyou.utils.CommonUtils
import com.reviyou.utils.CommonUtils._
import com.reviyou.utils.MongoDBTestUtils._
import play.api.test.{FakeRequest, PlaySpecification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by zhgirov on 24.05.14.
 */
@RunWith(classOf[JUnitRunner])
class BookmarkControllerSpec extends PlaySpecification {

  "the bookmark controller" should {

    "unbookmark" in withMongoDb { implicit app =>

      val profileId = BSONObjectID.generate.stringify

      Await.result(BookmarkDao.insert(Bookmark(None, profileId, CommonUtils.USER_ID)), Duration.Inf)

      val request = FakeRequest(DELETE, s"$REST_API/bookmark/profile/$profileId?user_id=$USER_ID&user_token=token")
      val response = route(request).get

      status(response) must equalTo(OK)

      val data = Json.parse(contentAsString(response)).as[JsObject]

      data.\("status").as[Int] must_== 0
    }
  }
}
