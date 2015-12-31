package com.reviyou.controllers.bookmark.get

import com.reviyou.models.Bookmark
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import com.reviyou.services.dao.BookmarkDao
import com.reviyou.utils.CommonUtils._
import com.reviyou.utils.MongoDBTestUtils._
import play.api.test.{FakeRequest, PlaySpecification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by zhgirov on 24.05.14.
 */
@RunWith(classOf[JUnitRunner])
class BookmarkControllerSpec extends PlaySpecification {

  "the bookmark controller" should {

    "get bookmarks" in withMongoDb { implicit app =>

      val profileId = BSONObjectID.generate

      createProfileWithJobs("profile", USER_ID, DateTime.now(), Some(profileId))

      Await.result(BookmarkDao.insert(Bookmark(None, profileId.stringify, USER_ID)), Duration.Inf)

      val request = FakeRequest(GET, s"$REST_API/bookmark?user_id=$USER_ID&user_token=token")
      val response = route(request).get

      status(response) must equalTo(OK)

      val data = Json.parse(contentAsString(response)).as[JsObject]
      val result = data.\("data").as[JsArray].value

      data.\("status").as[Int] must_== 0
      result must haveSize(1)
      result.head.\("_id").as[String] must_== profileId.stringify
    }
  }
}
