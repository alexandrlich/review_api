package com.reviyou.controllers.bookmark.add

import reactivemongo.bson.BSONObjectID
import play.api.test.Helpers._
import com.reviyou.utils.CommonUtils._
import com.reviyou.utils.MongoDBTestUtils._
import play.api.test.{FakeRequest, PlaySpecification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsObject, Json}

/**
 * Created by zhgirov on 24.05.14.
 */
@RunWith(classOf[JUnitRunner])
class BookmarkControllerSpec extends PlaySpecification {

  "the bookmark controller" should {

    "add new bookmark" in withMongoDb { implicit app =>

      val profileId = BSONObjectID.generate.stringify
      val bookmark = Json.obj(
        "user_id" -> USER_ID,
        "user_token" -> "token",
        "profile_id" -> profileId
      )
      val request = FakeRequest(POST, s"$REST_API/bookmark/profile/$profileId").withJsonBody(bookmark)
      val response = route(request).get

      status(response) must equalTo(OK)

      val data = Json.parse(contentAsString(response)).as[JsObject]

      data.\("status").as[Int] must_== 0
      data.\("data").as[JsObject].\("user_id").as[String] must_== USER_ID
      data.\("data").as[JsObject].\("profile_id").as[String] must_== profileId
    }
  }
}
