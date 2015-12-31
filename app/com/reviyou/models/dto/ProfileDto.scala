package com.reviyou.models.dto

import com.reviyou.models.{ProfileSkill, Job}
import play.api.libs.json.Json




import play.api.libs.json._
import play.api.libs.functional.syntax._
//used to  view profile
case class ProfileDto(_id: Option[String],
                      name: String,
                      initials:String,
                      email: Option[String],
                      url: Option[String],
                      user_id: String,//creator id
                      theme_name: String,//color
                      views_count: Long,//total views count(not just unique visits)
                      general_average_rank: Int,//rank per profile
                      votes_count: Int,//count of votes for general profile
                      general_vote: Option[Int],//previous vote by this user
                      comments_count:Int,
                      state: Int,
                      can_delete: Boolean,
                      is_bookmarked: Boolean,
                      is_author: Option[Boolean],
                      jobs: List[Job],
                      tags: Option[List[String]],
                      skills: Option[List[ProfileSkillDto]],
                      popular_index: Int) {


}

//includes previous vote of the current user who sees this profile
case class ProfileSkillDto(
                            id: String,//skill_id
                            is_custom: Boolean,
                            skill_name: String,
                            skill_average_rank: Int,
                            votes_count: Int,//count of votes for skill

                            vote_value: Option[Int])//previous_vote by this user

object ProfileDto {




  implicit val profileSkillDtoReads = Json.reads[ProfileSkillDto]
  implicit val profileSkillDtoWrites = Json.writes[ProfileSkillDto]

  import com.reviyou.models.Job
  import com.reviyou.models.Company

  implicit val companyReads = Json.reads[Company]
  implicit val companyWrites = Json.writes[Company]


  implicit val jobReads = Json.reads[Job]
  implicit val jobWrites = Json.writes[Job]




  implicit val profileDtoFormat = Json.format[ProfileDto]
  implicit val profileSkillDtoFormat = Json.format[ProfileSkillDto]

}

/*
case class JobRequest(
                       company: Option[String],
                       occupation: String,
                       start_date: String,
                       end_date: String)


object JobRequest {

  import com.reviyou.models.Profile._

  implicit val jobRequestFormat = Json.format[JobRequest]

}*/


//json request to create a new profile
case class ProfileRequest(
                      name: String,
                      email: String,
                      user_id: String,//creator id
                      user_token: Option[String] = None,
                      theme_name: String,//color
                      jobs: List[Job])

object ProfileRequest {

  import com.reviyou.models.Profile._

  implicit val profileRequestFormat = Json.format[ProfileRequest]
}




/*
  case class BookmarkProfileDto(_id: Option[String],
                              name: String,
                              theme_name: String,
                              url: Option[String],
                              last_job: Job,
                              general_average_rank: Option[Int]

                          )

object BookmarkProfileDto {

  import com.reviyou.models.Profile._

  implicit val bookmarkProfileDtoFormat = Json.format[BookmarkProfileDto]
}
*/
case class SearchProfileDto(_id: Option[String],
                              name: String,
                              initials:String,
                              theme_name: String,
                              email: Option[String],
                              url: Option[String],
                              comments_count: Int,
                              views_count: Long,
                              occupation: String, //just show 1
                              company: String, //company name
                              //last_job: Job,
                              general_average_rank: Int
                              )

object SearchProfileDto {

  import com.reviyou.models.Profile._

  implicit val searchProfileDtoFormat = Json.format[SearchProfileDto]
}

//tmp solution for older version of iphone(till we upgrade to 1.2.3 REST
case class PopularJobDTO(occupation:String, company:String)

object PopularJobDTO {

  import com.reviyou.models.Profile._

  implicit val searchPopularJobFormat = Json.format[PopularJobDTO]
}

case class PopularProfileDto(_id: Option[String],
                            name: String,
                            initials:String,
                            theme_name: String,
                            url: Option[String],
                            comments_count: Int,
                            views_count: Long,
                            last_job: PopularJobDTO,//all we care about is that it contains occupation and company(name)
                            general_average_rank: Int,
                            popular_index: Int = 0
                            )

object PopularProfileDto {

  import com.reviyou.models.Profile._

  implicit val popularProfileDtoFormat = Json.format[PopularProfileDto]
}