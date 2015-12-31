package com.reviyou.controllers.profile.findbyid

import com.reviyou.utils.MongoDBTestUtils._
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
import com.reviyou.models.{Job, ProfileSkill}
import com.reviyou.models.Profile._
import reactivemongo.bson.BSONObjectID

/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class ProfileControllerSpec extends PlaySpecification {

  "the profile controller" should {

    "retrieve profile by id" in withMongoDb { implicit app =>
      createProfile("profile1", USER_ID)
      val userId = BSONObjectID.generate.stringify
      Await.result(createUser(userId), Duration.Inf)

      val profiles = Await.result(ProfileDao.find(Json.obj()), Duration.Inf)
      //      profiles must haveSize(1)

      val profileId = profiles.head._id.get.stringify
      val request = FakeRequest(GET, s"$REST_API/profile/$profileId?user_id=$USER_ID&user_token=token")
      val response = route(request).get

      route(FakeRequest(GET, s"$REST_API/profile/${BSONObjectID.generate.stringify}?user_id=$userId&user_token=token")).get

      status(response) must equalTo(OK)

      val data = Json.parse(contentAsString(response)).as[JsObject]

      data.\("status").as[Int] must_== 0
      data.\("data").as[JsObject] must_== Json.obj(
        "_id" -> s"$profileId",
        "first_name" -> "profile1 first",
        "last_name" -> "profile1 last",
        "email" -> "p*****e1@mail.com",
        "user_id" -> s"$USER_ID",
        "theme_name" -> "red",
        "views_count" -> 100,
        "general_average_rank" -> 0,
        "votes_count" -> 0,
        "can_delete" -> true,
        "is_bookmarked" -> false,
        "is_author" -> true,
        "jobs" -> List[Job](),
        "skills" -> List[ProfileSkill](),
        "popular_index" -> 0)
    }

  }
}
