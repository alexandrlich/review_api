package com.reviyou.controllers.profile.delete

import com.reviyou.utils.MongoDBTestUtils._
import play.api.test._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.reviyou.utils.CommonUtils
import CommonUtils._
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import com.reviyou.models.Job

/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class ProfileControllerSpec extends PlaySpecification {

  "the profile controller" should {

    "delete profile" in withMongoDb { implicit app =>

      val profileId = BSONObjectID.generate
      createProfile("profileExist", CommonUtils.USER_ID, List[Job](), Some(profileId))

      val request = FakeRequest(DELETE, s"$REST_API/profile/${profileId.stringify}?user_id=${CommonUtils.USER_ID}&user_token=token")
      val response = route(request).get

      status(response) must equalTo(OK)

      val data = Json.parse(contentAsString(response)).as[JsObject]

      data.\("status").as[Int] must_== 0

    }

  }
}
