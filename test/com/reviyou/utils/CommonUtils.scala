package com.reviyou.utils

import reactivemongo.bson.BSONObjectID
import scala.concurrent.Await
import com.reviyou.services.dao.ProfileDao
import com.reviyou.models.{ProfileSkill, Profile, Job}
import org.joda.time.DateTime
import scala.concurrent.duration.Duration._
import scala.Some

/**
 * Created by zhgirov on 21.05.14.
 */
object CommonUtils {

  val REST_API = "/api/1.0/rest"
  val USER_ID = "5378de960b00000b00124b00"

  def createProfileWithJobs(profile: String,
                            userId: String,
                            currentDate: DateTime,
                            _id: Option[BSONObjectID] = None,
                            popular_idx: Int = 0
                             ) = {
    createProfile(profile, userId,
      List(Job(Some(s"first $profile company"), "", Some(currentDate.minusYears(1).getMillis), Some(currentDate.getMillis)),
        Job(Some(s"last $profile company"), "", Some(currentDate.getMillis), Some(currentDate.getMillis))), _id, popular_index = popular_idx)
  }

  def createProfileWithSkill(profileId: BSONObjectID, userId: String, skillId: String) = {
    createProfile(s"profile[id=$profileId]", userId, _id = Some(profileId), skills =
      Some(List(ProfileSkill(skillId, false, s"profile skill [$skillId]", Some(0), 0))))
  }

  def createProfile(profile: String,
                    userId: String,
                    jobs: List[Job] = List[Job](),
                    _id: Option[BSONObjectID] = None,
                    skills: Option[List[ProfileSkill]] = Some(List[ProfileSkill]()),
                    popular_index: Int = 0) = {
    Await.result(
      ProfileDao.insert(Profile(
        _id,
        s"$profile first", //first_name
        s"$profile last", //last_name
        Some(s"${profile.toLowerCase}@mail.com"), //email
        s"$userId", //created_user_id
        "red", //color
        100, // views count
        Some(0), // profile rank
        0, // votes count
        jobs,
        skills,
        popular_index
      )), Inf)
  }
}
