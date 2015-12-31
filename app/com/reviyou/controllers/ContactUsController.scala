package com.reviyou.controllers

import com.reviyou.common.RestStatusCodes._
import com.reviyou.common.{Utils, RestStatusCodes}
import com.reviyou.common.RestStatusCodes
import com.reviyou.common.Utils
import com.reviyou.controllers.actions.AuthenticatedAction
import com.reviyou.services.{ProfileService, ContactUsService, SkillService}
import org.joda.time.DateTime

import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import com.reviyou.models._
import com.reviyou.services.dao.{ContactUsDao, CommentDao, UserDao, SkillDao}

import scala.concurrent.Future
import scala.util.{Right, Left}


/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import scala.language.postfixOps
import com.reviyou.models.Profile._

//TODO: rename to separate from Feedback=ContactUs functionality
object ContactUsController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)

  //request to add custom skill to profile
  def addCustomSkill(profile_id: String) = AuthenticatedAction.async(parse.json) {
    log.trace(s"addCustomSkillRequest, profile_id: $profile_id")

      request =>
      val cObj = request.body.as[JsObject].value
      val user_id = cObj.get("user_id").get.as[String]
      val skill_name = cObj.get("skill_name").get.as[String]

      Utils.validInput(skill_name) match {
        //validation
        case false => errorAsyncResponse(RestStatusCodes.ERROR_VALID_INPUT_LIMIT, s"Size of the skill name is not supported.")
        case true => {

          ContactUsService.addCustomSkill(profile_id, skill_name, user_id)
            .map {
            case Left(left) => errorResponse(ERROR_INSERT_OBJ_TO_DB, left.message)
            case Right(right) =>  successResponse(Json.toJson(right).as[JsObject])
          }
        }
      }

  }


  //request to delete profile
  def deleteProfile(profile_id: String) = AuthenticatedAction.async(parse.json) {
    log.trace(s"deleteProfileRequest, profile_id: $profile_id")

    request =>
      val cObj = request.body.as[JsObject].value
      val user_id = cObj.get("user_id").get.as[String]

      ContactUsService.deleteProfile(profile_id,user_id)
        .map {
        case Left(left) => errorResponse(ERROR_INSERT_OBJ_TO_DB, left.message)
        case Right(right) =>  successResponse(Json.toJson(right).as[JsObject])
      }

  }

  //request to add job
  def addJob(profile_id: String) = AuthenticatedAction.async(parse.json) {
    log.trace(s"addJob, profile_id: $profile_id")

    request =>
      //TODO: leverage JobRequest format(see profile creation
      val cObj = request.body.as[JsObject].value
      val user_id = cObj.get("user_id").get.as[String]
      //val company_name = cObj.get("company_name").get.as[String]
      //val occupation = cObj.get("occupation").get.as[String]


      val start_dateS = cObj.get("start_date").get.as[Long]
      val end_dateS = cObj.get("end_date").get.as[Long]


      val company = cObj.get("company_name").get.as[String]
      val occupation = cObj.get("occupation").get.as[String]

      val start_dateO = if(start_dateS ==0 ) None else Some(start_dateS)
      val end_dateO = if(end_dateS ==0 ) None else Some(end_dateS)


      //TODO: move to the formatter of model Request object (see how it's done for profile createion)

/*
      val startDate = start_dateS match {
        case "" => None
        case x => Option(x.toLong)
      }
      val endDate = end_dateS match {
        case "" => None
        case x => Option(x.toLong)
      }
*/
      // if empty string - convert to long
      (Utils.validInput(occupation)&&Utils.validInput(company)) match {
        //validation
        case false => errorAsyncResponse(RestStatusCodes.ERROR_VALID_INPUT_LIMIT, s"Size of the occupation or company name is not supported.")
        case true => {

          ContactUsService.addJob(profile_id, company, start_dateO,  end_dateO, occupation, user_id)
            .map{
            case Left(left) => errorResponse(ERROR_INSERT_OBJ_TO_DB, left.message)
            case Right(right) =>  successResponse(Json.toJson(right).as[JsObject])
          }
        }
      }
  }

}
