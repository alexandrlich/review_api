package com.reviyou.controllers.profile.skill.add

import com.reviyou.utils.MongoDBTestUtils._
import play.api.test._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.reviyou.utils.CommonUtils
import CommonUtils._
import play.api.libs.json.{JsObject, Json}
import com.reviyou.services.dao.{ProfileDao, SkillDao}
import reactivemongo.bson.BSONObjectID
import java.util.Date
import com.reviyou.models.{Job, Profile, ProfileSkill, Skill}
import scala.concurrent.duration.Duration
import scala.concurrent.Await

/* Implicits */

import scala.concurrent.ExecutionContext.Implicits._

//import play.modules.reactivemongo.json.ImplicitBSONHandlers._
/**
 * Created by zhgirov on 17.05.14.
 */
@RunWith(classOf[JUnitRunner])
class ProfileControllerSpec extends PlaySpecification {

  "the profile controller" should {

    "add skill profile" in withMongoDb { implicit app =>
      val profileId = BSONObjectID.generate
      val skillId = BSONObjectID.generate
      createProfile("profile", CommonUtils.USER_ID, List[Job](), Some(profileId))

      SkillDao.insert(Skill(Some(skillId), "skillName"))

      val skill = Json.obj(
        "user_id" -> CommonUtils.USER_ID,
        "user_token" -> "token",
        "skill_id" -> s"${skillId.stringify}"
      )

      val request = FakeRequest(POST, s"$REST_API/profile/${profileId.stringify}/skill").withJsonBody(skill)
      val result = route(request).get

      status(result) must equalTo(OK)

      val data = Json.parse(contentAsString(result)).as[JsObject]

      data.\("status").as[Int] must_== 0
      data.\("data").as[JsObject] must_== Json.obj()

      val profile = Await.result(ProfileDao.findById(profileId).map(p => p.get), Duration.Inf)

      profile.skills.get must haveSize(1)
      val firstSkill = profile.skills.get.head

      firstSkill.skill_id must_== skillId.stringify
      firstSkill.is_custom must_== false
      firstSkill.skill_name must_== "skillName"

    }

  }
}
