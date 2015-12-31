package com.reviyou.controllers.vote

import java.util.Date

import com.reviyou.common.RestStatusCodes._
import com.reviyou.controllers.filters.LoggingFilter._
import com.reviyou.models.{ProfileSkill, Job, Vote}
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsObject, Json}
import play.api.test._
import reactivemongo.bson.BSONObjectID
import com.reviyou.services.dao.{ProfileDao, VoteDao}
import com.reviyou.services.db.DBQueryBuilder
import com.reviyou.utils.CommonUtils
import CommonUtils._
import com.reviyou.utils.MongoDBTestUtils._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

import play.modules.reactivemongo.json.BSONFormats._

/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class VoteControllerSpec extends PlaySpecification {

  private val log = LoggerFactory.getLogger(getClass)

  "the vote controller" should {

    "add new" in withMongoDb { implicit app =>

      val profileId1 = BSONObjectID.generate.stringify
      val profileId2 = BSONObjectID.generate.stringify
      val skillId = BSONObjectID.generate.stringify

      createProfile(profileId1, USER_ID)
      createProfile(profileId2, USER_ID)

      val u1 = BSONObjectID.generate.stringify

      Await.result(
        VoteDao.insert(
          Vote(6, System.currentTimeMillis, profileId1, None, u1)
        ),
        Duration.Inf)

//      Await.result(
//        VoteDao.insert(
//          Vote(3, System.currentTimeMillis, profileId1, None, USER_ID)
//        ),
//        Duration.Inf)

//      Await.result(
//        VoteDao.insert(
//          Vote(6, System.currentTimeMillis, profileId2, None, USER_ID)
//        ),
//        Duration.Inf)

      Await.result(
        VoteDao.insert(
          Vote(2, System.currentTimeMillis, profileId2, None, u1)
        ),
        Duration.Inf)


      val request = FakeRequest(POST, s"$REST_API/vote/$profileId1").withBody(
        Json.obj("user_id" -> USER_ID,
          "user_token" -> "token",
          "general_vote" -> 3
        )
      )
      val result = route(request).get

      status(result) must equalTo(OK)

      val response = Json.parse(contentAsString(result)).as[JsObject]

      println(s"obj: ${response.\("data")}")
      response.\("status").as[Int] must_== 0
      response.\("data").as[JsObject] must_== Json.obj(
        "general_average_rank" -> 45,
        "general_vote" -> (9d / 2d)
      )
      val profileId = BSONObjectID.generate
      val skillId1 = BSONObjectID.generate.stringify

      createProfileWithSkill(profileId, USER_ID, skillId1)

      val profForSkillVote = Await.result(ProfileDao.find(
        DBQueryBuilder.and(
          Json.obj("_id" -> BSONObjectID(profileId.stringify)),
          Json.obj("skills.skill_id" -> skillId1)
        )), Duration.Inf)

      val voteSkillVal = FakeRequest(POST, s"$REST_API/vote/${profileId.stringify}/$skillId1").withBody(
        Json.obj("user_id" -> USER_ID,
          "user_token" -> "token",
          "vote_value" -> 10
        )
      )
      val resultSkillVote = route(voteSkillVal).get

      status(resultSkillVote) must equalTo(OK)

      val responseSkillVote = Json.parse(contentAsString(resultSkillVote)).as[JsObject]

      println(s"obj: ${responseSkillVote.\("data")}")
      responseSkillVote.\("status").as[Int] must_== 0
      responseSkillVote.\("data").as[JsObject] must_== Json.obj(
        "skill_average_rank" -> 100
      )

      for (i <- 3 to 4) {
        val userId = BSONObjectID.generate.stringify

        Await.result(VoteDao.insert(Vote(i, System.currentTimeMillis, profileId.stringify, Some(skillId1), userId)), Duration.Inf)
        //sum += i
      }

      // Check will be changed estimation of skill if a user who voted before changes his estimation for skill of profile
      val requestVoteTwice = FakeRequest(POST, s"$REST_API/vote/${profileId.stringify}/$skillId1").withBody(
        Json.obj("user_id" -> USER_ID,
          "user_token" -> "token",
          "vote_value" -> 5
        )
      )
      val resultlVoteTwice = route(requestVoteTwice).get

      status(resultlVoteTwice) must equalTo(OK)

      val responseVoteTwice = Json.parse(contentAsString(resultlVoteTwice)).as[JsObject]

      responseVoteTwice.\("status").as[Int] must_== 0
      responseVoteTwice.\("data").as[JsObject] must_== Json.obj(
        "skill_average_rank" -> 40
      )

      /*val profileOther = BSONObjectID.generate.stringify
      val userId = BSONObjectID.generate.stringify
      createProfile("profileOther",
        USER_ID,
        _id = Some(BSONObjectID.apply(profileOther))
      )

      for (i <- 1 to 5) {
        Await.result(VoteDao.insert(Vote(i, System.currentTimeMillis, profileOther, None, userId)), Duration.Inf)
      }

      val request = FakeRequest(POST, s"$REST_API/vote/$profileOther").withBody(
        Json.obj("user_id" -> USER_ID,
          "user_token" -> "token",
          "general_vote" -> 2
        )
      )
      val result = route(request).get

      status(result) must equalTo(OK)

      val response = Json.parse(contentAsString(result)).as[JsObject]

      println(s"obj: ${response.\("data")}")
      response.\("status").as[Int] must_== 0
      response.\("data").as[JsObject] must_== Json.obj(
        "general_average_rank" -> 100,
        "general_vote" -> (17d / 6d)
      )*/
    }
  }
}
