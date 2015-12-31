package com.reviyou.models

import play.api.Play
import play.api.libs.json._
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID

import scalaz.Value
import play.api.Play.current


case class Profile(override var _id: Option[BSONObjectID],
                   name: String,
                   email: Option[String],
                   image_url: Option[String],
                   big_image_url: Option[String],
                   date_of_birth: Option[String],
                   private val creator_user_id: Option[String],//profile_creator
                   private val theme_name: Option[String],//color of the profile (1-15)
                   private val views_count: Option[Long],//total views count(not just unique visits)
                   comments_count:Option[Int],
                   private val general_average_rank: Option[Int], //general profile rank
                   private val votes_count:Option[Int], //count of votes for general of the profile
                   jobs: List[Job],
                   tags: Option[List[String]],
                   skills: Option[List[ProfileSkill]],
                   private val popular_index: Option[Int],//currently == unique visitors
                   private val state:Option[Int],
                   override var updated: Option[DateTime] = None,
                   override var created: Option[DateTime] = None
                    ) extends TemporalModel {

  def getTheme:String = {
    if(theme_name.isEmpty)
      ((Math.abs(_id.getOrElse(BSONObjectID.generate).hashCode) % 7) +1).toString
    else theme_name.get
  }

  def getCreatorUserId:String = { if (creator_user_id.isEmpty) "adminId" else creator_user_id.get}
  def getViewCount:Long = { if (views_count.isEmpty) 0 else views_count.get}
  def getAverageRank:Int = { if (general_average_rank.isEmpty) 0 else general_average_rank.get}
  def getVotesCount:Int = { if (votes_count.isEmpty) 0 else votes_count.get}

  def getPopularInd:Int = { if (popular_index.isEmpty) 0 else popular_index.get}

  def getState:Int = { if (state.isEmpty) ProfileState.Waived else state.get}

  def getInitials:String = {name.split(" ").map(_.head).mkString takeRight 2 }

  def getLastOccupation:String = {if(jobs.isEmpty) "" else jobs.head.occupation.getOrElse("") }
  def getLastCompanyName:String = {
    jobs.isEmpty match {
      case false => {
        if(jobs.size==0) "" else jobs.head.companies.isEmpty match {
          case false => {
            val cList = jobs.head.companies.get

            cList.isEmpty match {
              case true =>""
              case false => {
                val companyName = cList.head.name
                companyName.isDefined match {
                  case true =>companyName.get
                  case false =>""
                }
              }
            }

          }
          case true => ""
        }
      }
      case true => ""

    }
  }



  //url in the database
  //def getImageUrl = {if(big_image_url.isDefined) big_image_url else image_url}

  val profileImageHost = Play.application.configuration.getString("web.apiimage.profile").getOrElse("")


  def getSmallImageUrl = {

    Some(profileImageHost + _id.get.stringify + "_0.jpg")
  }

  def getLargeImageUrl = {

    Some(profileImageHost + _id.get.stringify + "_b_0.jpg")
  }

}

object ProfileState  {
  //type String = Value
  /**
   * deleted\archived profiles in the system, should be ignored by the functionality
   */
  val Deleted:Int = -1
  /**
   * View only.
   * Pending email approval before it can be used for reviews
   */
  val Pending :Int = 1
  /**
   * Approval is not required(celebrity profiles or created by admins profiles
   */
  val Waived:Int = 2//for celebrities we don't require it
  /**
   * Actively used profiles, approved by the owner and available everywhere
   */
  val Approved:Int = 3
  /**
   * Rejected by email address owner
   */
  val Rejected:Int = 4
}


case class Job(
                companies: Option[List[Company]],
                occupation: Option[String],
                start_date: Option[Long] = None,
                end_date: Option[Long] = None,
                isCurrent: Option[Boolean] = Some(false))


case class Company(
                    name: Option[String],
                    start_date: Option[String] = None,
                    end_date: Option[String] = None)

case class ProfileSkill(
                         skill_id: String,
                         is_custom: Boolean,
                         skill_name: String,
                         skill_average_rank: Option[Int],
                         votes_count: Int)

//rank: Option[Int])

object Profile {




  import play.modules.reactivemongo.json.BSONFormats._


  // For MongoDB serialization
  //implicit val listJobsFormat = Json.format[Job]


  //implicit val listCompaniesFormat = Json.format[Company]
  import play.modules.reactivemongo.json.BSONFormats._
  implicit val company = new Format[Company] {
    override def reads(json: JsValue): JsResult[Company] = JsSuccess(Company(
      (json \ "at").asOpt[String],//name of the company\movie
      (json \ "sd").asOpt[String],
      (json \ "ed").asOpt[String]

    ))

    override def writes(company: Company): JsValue = {
      Json.obj("at" -> company.name,//name of the company\movie
        "sd" -> company.start_date,//start date
        "ed" -> company.end_date)
    }
  }

  implicit val jobFormat = new Format[Job] {
    override def reads(json: JsValue): JsResult[Job] = JsSuccess(Job(
        (json \ "os").asOpt[List[Company]],//company(or list of movies)
        (json \ "o").asOpt[String],//occupation(actress)
        (json \ "start_date").asOpt[Long],
        (json \ "end_date").asOpt[Long],
        (json \ "isCurrent").asOpt[Boolean]
      )

    )

    override def writes(job: Job): JsValue = {
      Json.obj("os" -> job.companies,
        "o" -> job.occupation,
        "start_date" -> job.start_date,
        "end_date" -> job.end_date,
        "isCurrent" -> job.isCurrent
      )
    }
  }



 // implicit val profileStateFormat = Json.format[ProfileState]
  implicit val skillFormat = Json.format[ProfileSkill]
//  implicit val profileFormat = Json.format[Profile]

  implicit val profileFormat = new Format[Profile] {

    override def reads(json: JsValue): JsResult[Profile] = JsSuccess(Profile(
      (json \ "_id").asOpt[BSONObjectID],
      (json \ "n").as[String],//name
      (json \ "email").asOpt[String],
      (json \ "i").asOpt[String],// image
      (json \ "bi").asOpt[String],//big image
      (json \ "db").asOpt[String],//birth
      (json \ "user_id").asOpt[String],
      (json \ "tn").asOpt[String],//theme_name
      (json \ "vwc").asOpt[Long],//views_count
      (json \ "сtсt").asOpt[Int],// commentsCount
      (json \ "gar").asOpt[Int],//general_average_rank
      (json \ "vtc").asOpt[Int],//votes_count
      (json \ "os").as[List[Job]],
      (json \ "tags").asOpt[List[String]],
      (json \ "skills").asOpt[List[ProfileSkill]],
      (json \ "pi").asOpt[Int],//popular_index
      (json \ "state").asOpt[Int]

    )

    )

    override def writes(profile: Profile): JsValue = {
      Json.obj("_id" -> profile._id,
        "n" -> profile.name,
        "email" -> profile.email,
        "i" -> profile.image_url,
        "bi" -> profile.big_image_url,
        "db" -> profile.date_of_birth,
        "user_id" -> profile.creator_user_id,
        "tn" -> profile.theme_name,
        "vwc" -> profile.views_count,
        "сtсt" -> profile.comments_count,
        "gar" -> profile.general_average_rank,
        "vtc" -> profile.votes_count,
        "os" -> profile.jobs,
        "skills" -> profile.skills,
        "pi" -> profile.popular_index,
        "state" -> profile.state,
        "os" -> profile.jobs,
        "tags" -> profile.tags
      )

    }
  }


}