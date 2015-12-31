package com.reviyou.controllers.profile.popular

import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.test._
import reactivemongo.bson.BSONObjectID
import com.reviyou.utils.CommonUtils._
import com.reviyou.utils.MongoDBTestUtils._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class ProfileControllerSpec extends PlaySpecification {

  "the profile controller" should {

    "popular profiles" in withMongoDb { implicit app =>

      val profileId0 = BSONObjectID("5430649c0900001e00478ee6")//.generate
      val profileId1 = BSONObjectID("5430649c0900001c00478ee6")//.generate
      val profileId2 = BSONObjectID("5430649c0900001d00478ee6")//.generate
      createProfileWithJobs("profile1", USER_ID, DateTime.now(), _id = Some(profileId0))
      createProfileWithJobs("profile2", USER_ID, DateTime.now(), _id = Some(profileId1))

      for (i <- 1 to 52) {
        val userId = BSONObjectID.generate.stringify

        Await.result(createUser(userId), Duration.Inf)
        status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId&user_token=token")).get) must equalTo(OK)
        status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId&user_token=token")).get) must equalTo(OK)
      }

      for (i <- 1 to 60) {
        val userId = BSONObjectID.generate.stringify

        Await.result(createUser(userId), Duration.Inf)
        status(route(FakeRequest(GET, s"$REST_API/profile/${profileId1.stringify}?user_id=$userId&user_token=token")).get) must equalTo(OK)
        //        status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId&user_token=token")).get) must equalTo(OK)
      }

      for (i <- 1 to 40) {
        val userId = BSONObjectID.generate.stringify

        Await.result(createUser(userId), Duration.Inf)
        status(route(FakeRequest(GET, s"$REST_API/profile/${profileId2.stringify}?user_id=$userId&user_token=token")).get) must equalTo(OK)
        //        status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId&user_token=token")).get) must equalTo(OK)
      }
      //      val userId0 = BSONObjectID.generate.stringify
      //      val userId1 = BSONObjectID.generate.stringify
      //      val userId2 = BSONObjectID.generate.stringify
      //      val userId3 = BSONObjectID.generate.stringify
      //      val userId4 = BSONObjectID.generate.stringify
      //
      //
      //      Await.result(createUser(userId1), Duration.Inf)
      //      Await.result(createUser(userId2), Duration.Inf)
      //      Await.result(createUser(userId3), Duration.Inf)
      //      Await.result(createUser(userId4), Duration.Inf)
      //
      //      status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId0&user_token=token")).get) must equalTo(OK)
      //      status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId1&user_token=token")).get) must equalTo(OK)
      //      status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId1&user_token=token")).get) must equalTo(OK)
      //      status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId2&user_token=token")).get) must equalTo(OK)
      //      status(route(FakeRequest(GET, s"$REST_API/profile/${profileId0.stringify}?user_id=$userId3&user_token=token")).get) must equalTo(OK) // popular_index 4
      //      status(route(FakeRequest(GET, s"$REST_API/profile/${profileId1.stringify}?user_id=$userId3&user_token=token")).get) must equalTo(OK)
      //      status(route(FakeRequest(GET, s"$REST_API/profile/${profileId1.stringify}?user_id=$userId4&user_token=token")).get) must equalTo(OK)
      //      status(route(FakeRequest(GET, s"$REST_API/profile/${profileId1.stringify}?user_id=$userId4&user_token=token")).get) must equalTo(OK) // popular_index 2
      //
      val request = FakeRequest(GET, s"$REST_API/popular?user_id=$USER_ID&user_token=token")
      val response = route(request).get

      status(response) must equalTo(OK)

      val data = Json.parse(contentAsString(response)).as[JsObject]
      val result = data.\("data").as[JsArray].value

      data.\("status").as[Int] must_== 0
      result must haveSize(2)
      result.head.\("_id").as[String] must_== profileId1.stringify
      result.head.\("popular_index").as[Int] must_== 60
      result.tail.head.\("_id").as[String] must_== profileId0.stringify
      result.tail.head.\("popular_index").as[Int] must_== 52
    }
  }
}
