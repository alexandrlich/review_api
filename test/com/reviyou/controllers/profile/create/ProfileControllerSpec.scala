package com.reviyou.controllers.profile.create

import com.reviyou.utils.MongoDBTestUtils._
import org.joda.time.DateTime
import scala.concurrent.Await
import com.reviyou.services.dao.ProfileDao
import scala.concurrent.duration._
import play.api.libs.json.Json
import play.api.test._
import play.api.libs.json.JsObject
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.reviyou.utils.CommonUtils
import CommonUtils._
import java.util.Date
import com.reviyou.common.RestStatusCodes._
import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID
import com.reviyou.models.Job

/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class ProfileControllerSpec extends PlaySpecification {

  "the profile controller" should {

    "create new profile" in withMongoDb { implicit app =>

      val profileId = BSONObjectID.generate
      createProfile("Profileexist", USER_ID, List[Job](), Some(profileId))

      val existing = Json.obj(
        "first_name" -> "First_name",
        "last_name" -> "Last_name",
        "user_id" -> USER_ID,
        "user_token" -> "token",
        "email" -> "profileexist@mail.com",
        "theme_name" -> "theme1",
        "jobs" -> Json.arr(
          Json.obj(
            "company" -> "Company1",
            "occupation" -> "occupation",
            "start_date" -> "", //DateTime.now.getMillis,
            "end_date" -> DateTime.now.getMillis
          )
        ),
        "skills" -> Json.arr()
      )

      val request = FakeRequest(POST, s"$REST_API/profile").withJsonBody(existing)
      val response = route(request).get

      status(response) must equalTo(OK)

      val data = Json.parse(contentAsString(response)).as[JsObject]

      data.\("status").as[Int] must_== ERROR_PROFILE_EXISTS

      val newProfile = Json.obj(
        "user_id" -> s"${CommonUtils.USER_ID}",
        "user_token" -> "token",
        "first_name" -> "First_name",
        "last_name" -> "Last_name",
        "email" -> "profile1@mail.com",
        "theme_name" -> "theme1",
        "visible" -> true,
        "jobs" -> Json.arr(
          Json.obj(
            "company" -> "Company1",
            "occupation" -> "occupation",
            "start_date" -> DateTime.now.getMillis,
            "end_date" -> DateTime.now.getMillis
          )
        ),
        "skills" -> Json.arr()
      )

      val requestNew = FakeRequest(POST, s"$REST_API/profile").withJsonBody(newProfile)
      val result = route(requestNew).get

      status(result) must equalTo(OK)

      val dataNew = Json.parse(contentAsString(result)).as[JsObject]

      dataNew.\("status").as[Int] must_== 0
      dataNew.\("data").as[JsObject] mustNotEqual None
      dataNew.\("data").as[JsObject].\("_id") mustNotEqual None
    }

  }
}
