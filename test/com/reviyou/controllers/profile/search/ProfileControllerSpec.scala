package com.reviyou.controllers.profile.search

import com.reviyou.utils.MongoDBTestUtils._
import play.api.test._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.{JsArray, JsObject, Json}
import com.reviyou.utils.CommonUtils
import CommonUtils._
import org.joda.time.DateTime

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._

/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class ProfileControllerSpec extends PlaySpecification {

  "the profile controller" should {

    "search profiles" in withMongoDb { implicit app =>
      val profileId = BSONObjectID.generate
      createProfileWithJobs("profile1", "user1", DateTime.now(), Some(profileId))
      createProfileWithJobs("profile2", "user2", DateTime.now())

      val request = FakeRequest(GET, s"$REST_API/search?query=profile1@mail.com&user_id=${CommonUtils.USER_ID}&user_token=token")
      val response = route(request).get

      status(response) must equalTo(OK)

      val data = Json.parse(contentAsString(response)).as[JsObject]
      val result = data.\("data").as[JsArray].value

      data.\("status").as[Int] must_== 0
      result must haveSize(1)
      result.head.as[JsObject] must_== Json.obj(
        "_id" -> profileId.stringify,
        "first_name" -> "profile1 first",
        "last_name" -> "profile1 last",
        "email" -> "p*****e1@mail.com",
        "popular_index" -> 0,
        "last_company" -> "last profile1 company")
    }
  }
}
