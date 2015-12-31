package com.reviyou.controllers.comments

import com.reviyou.utils.MongoDBTestUtils._
import play.api.libs.json.{JsArray, Json, JsObject}
import play.api.test._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.reviyou.utils.CommonUtils
import CommonUtils._
import reactivemongo.bson.BSONObjectID
import com.reviyou.models.Comment
import org.joda.time.DateTime
import com.reviyou.services.dao.CommentDao
import scala.None
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class CommentControllerSpec extends PlaySpecification {

  "the comment controller" should {

    "save comment and then get it (2 in 1)" in withMongoDb { implicit app =>

      val profileId = BSONObjectID.generate
      val commentId = BSONObjectID.generate
      createProfileWithJobs("profileComment", USER_ID, DateTime.now(), Some(profileId), popular_idx = 41)

      val creationTime = DateTime.now().plusDays(1).getMillis
      val expectedComment = Comment(
        Some(commentId),
        profileId.stringify,
        USER_ID,
        "firstExpected",
        "lastExpected",
        "expected comment",
        creationTime//,
        //Some(
          //List(
          //CommentVote(USER_ID, "group1"),
          //CommentVote(USER_ID, "group2"),
          //CommentVote("1234567890", "group2"),
          //CommentVote(USER_ID, "group3"),
          //CommentVote("1234567890", "GroupIsNotVoted")
          //)
        //)
      )

      val input = Json.obj(
        "user_id" -> USER_ID,
        "user_token" -> "token",
        "text" -> "comment text"
      )

      val requestSave = FakeRequest(POST, s"$REST_API/comment/${profileId.stringify}").withJsonBody(input)
      val responseSave = route(requestSave).get

      status(responseSave) must equalTo(OK)

      val dataSave = Json.parse(contentAsString(responseSave)).as[JsObject]

      dataSave.\("status").as[Int] must_== 0
      dataSave.\("data").as[JsObject].\("text").as[String] must_== "comment text"
      dataSave.\("data").as[JsObject].\("profile_id").as[String] must_== profileId.stringify

      //Second part for verifying get comments for profile

      Await.result(CommentDao.insert(expectedComment), Duration.Inf)

      val requestGet = FakeRequest(GET, s"$REST_API/comment/${profileId.stringify}?user_id=$USER_ID&user_token=token")
      val responseGet = route(requestGet).get

      val dataGet = Json.parse(contentAsString(responseGet)).as[JsObject]

      dataGet.\("data").as[JsArray].value.toList must not be empty

      val data = dataGet.\("data").as[JsArray].value.head

      dataGet.\("status").as[Int] must_== 0
      data.\("profile_id").as[String] must_== profileId.stringify
      data.\("user_first_name").as[String] must_== "firstExpected"
      data.\("user_last_name").as[String] must_== "lastExpected"
      data.\("text").as[String] must_== "expected comment"
      data.\("create_time").as[Long] must_== creationTime
      data.\("user_image_url").as[String] must endWith("/user_profile_image") //full image url is http(s)://domain/..../user_profile_image

      val request = FakeRequest(GET, s"$REST_API/popular?user_id=$USER_ID&user_token=token")
      val response = route(request).get

      status(response) must equalTo(OK)

      val newData = Json.parse(contentAsString(response)).as[JsObject]
      val result = newData.\("data").as[JsArray].value

      newData.\("status").as[Int] must_== 0
      result.head.\("comments_count").as[Int] must_== 2


      val requestRemoveVote = FakeRequest(POST, s"$REST_API/comment/${commentId.stringify}/group1").withJsonBody(input)
      val responseRemoveVote = route(requestRemoveVote).get

      status(responseRemoveVote) must equalTo(OK)

      val removedVoteData = Json.parse(contentAsString(responseRemoveVote)).as[JsObject].\("data")

      println(s"${removedVoteData.\("comment_votes")}")
      removedVoteData.\("comment_votes").as[JsObject].keys must haveSize(3)
      removedVoteData.\("comment_votes").\("group1").asOpt[String] must beEmpty

      val requestAddVote = FakeRequest(POST, s"$REST_API/comment/${commentId.stringify}/groupChanged").withJsonBody(input)
      val responseAddVote = route(requestAddVote).get

      status(responseAddVote) must equalTo(OK)

      val addedVoteData = Json.parse(contentAsString(responseAddVote)).as[JsObject].\("data")

      addedVoteData.\("comment_votes").as[JsObject].keys must haveSize(4)

    }

  }
}
