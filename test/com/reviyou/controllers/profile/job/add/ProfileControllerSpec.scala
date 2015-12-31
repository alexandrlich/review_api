package com.reviyou.controllers.profile.job.add

import com.reviyou.utils.MongoDBTestUtils._
import play.api.test._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.reviyou.utils.CommonUtils
import CommonUtils._
import play.api.libs.json.{JsObject, Json}
import com.reviyou.services.dao.{ProfileDao, SkillDao}
import reactivemongo.bson.BSONObjectID
import com.reviyou.models.{Job, Skill}
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import org.joda.time.DateTime

/* Implicits */

import scala.concurrent.ExecutionContext.Implicits._

//import play.modules.reactivemongo.json.ImplicitBSONHandlers._
/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class ProfileControllerSpec extends PlaySpecification {

  "the profile controller" should {

    "add job profile" in withMongoDb { implicit app =>
      val profileId = BSONObjectID.generate
      //val skillId = BSONObjectID.generate
      createProfile("profile", CommonUtils.USER_ID, List[Job](), Some(profileId))

      val startDate = DateTime.now().minusYears(1).getMillis
      val endDate = DateTime.now().getMillis
      val job = Json.obj(
        "user_id" -> CommonUtils.USER_ID,
        "user_token" -> "token",
        "company_name" -> "testCompany",
        "occupation" -> "testOccupation",
        "start_date" -> startDate,
        "end_date" -> endDate
      )

      val request = FakeRequest(POST, s"$REST_API/profile/${profileId.stringify}/job").withJsonBody(job)
      val result = route(request).get

      status(result) must equalTo(OK)

      val data = Json.parse(contentAsString(result)).as[JsObject]

      data.\("status").as[Int] must_== 0
      data.\("data").as[JsObject] must_== Json.obj()

      val profile = Await.result(ProfileDao.findById(profileId).map(p => p.get), Duration.Inf)

      profile.jobs must haveSize(1)
      val firstJob = profile.jobs.head

      firstJob.company must_== job.value.get("company_name").get.asOpt[String]
      firstJob.occupation must_== job.value.get("occupation").get.as[String]
      firstJob.start_date must_== job.value.get("start_date").get.as[Long]
      firstJob.end_date must_== job.value.get("end_date").get.as[Long]

    }

  }
}
