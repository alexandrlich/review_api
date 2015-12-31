package com.reviyou.controllers

import com.reviyou.common.RestStatusCodes._
import com.reviyou.common.Utils
import com.reviyou.controllers.actions.{LoggingAction, AuthenticatedAction}
import com.reviyou.models.{ProfileState, Job}

import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import com.reviyou.services.{EmailService, BaseServiceO, ProfileService}
import com.reviyou.common.RestStatusCodes
import com.reviyou.models.dto.ProfileRequest
import play.api.mvc.{Action, SimpleResult}

import scala.concurrent.Future

// Reactive Mongo plugin, including the JSON-specialized collection

import com.reviyou.models.Profile._

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import scala.language.postfixOps


object ProfileController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)


  def convertRequestToProfile(request: JsObject): JsValue = {
    Json.obj(
      "first_name" -> request.value.get("first_name").get,
      "last_name" -> request.value.get("last_name").get,
      "email" -> request.value.get("email").get,
      "user_id" -> request.value.get("user_id").get,
      "general_average_rank" -> 0,
      "current_user_general_vote" -> 0,
      "jobs" -> request.value.get("jobs").get,
      "comments" -> Json.arr(),
      "skills" -> request.value.get("skills").get
    )
  }

  def searchProfiles(query: String, tags:String, offset: Int) = AuthenticatedAction.async {
    log.trace(s"searchProfiles, tags: $tags ")

    ProfileService.searchProfiles(query,convertToList(tags), offset) map {
      list =>
        successResponse(JsArray(list))
    }
  }

  def convertToList(tags:String): List[String] = {
    tags.contains(",") match {
      case false =>List()
      case true =>(tags split ",").toList
    }


  }

  /**
   * If not famous - it should be in pending approval state and email is send to the profile associated email.
   * @return
   */
  def createProfile = AuthenticatedAction.async(parse.json) {
    request =>
      log.trace("createProfile: "+ request.body)

      val profileRequest = request.body.as[ProfileRequest]

      (Utils.validInput(profileRequest.email)&&
        Utils.validInput(profileRequest.name)&&
        validJobs(profileRequest.jobs)
        ) match {
        //validation
        case false => errorAsyncResponse(RestStatusCodes.ERROR_VALID_INPUT_LIMIT, s"Size of one of the profile fields is not supported.")
        case true => {

          ProfileService.createPending(profileRequest).map {
            result =>
              //response(result._1, result._2)
              response(result)
          }
        }
      }
  }

  def validJobs(jobs: List[Job]): Boolean = {
    jobs map {
      job =>

        Utils.validInput(job.occupation.get) &&
        Utils.validInput(job.companies.get(0).name.get)

    }

    return true
  }

  //return consolidated profile object to view
  def findById(id: String, userId: String) = AuthenticatedAction.async {
    ProfileService.getProfileViewById(id, userId) map (
      res=>response(res))

  }

  //add existing common skill to profile
  def addSkill(profileId: String) = AuthenticatedAction.async(parse.json) { request =>
    log.trace(s"addSkill, profileId : $profileId")

    val skillId = request.body.as[JsObject].value.get("skill_id").get.as[String]

    log.debug(s"skillId : $skillId")

    val userId = request.body.as[JsObject].value.get("user_id").get.as[String]

    ProfileService.addSkill(skillId, profileId, userId).map (res => response(res))

  }

  def addJob(profileId: String) = AuthenticatedAction.async(parse.json) { request =>
    log.trace(s"addJob, profileId : $profileId")

    //TODO: move to the formatter of model Request object (see how it's done for profile createion)
    val data = request.body.as[JsObject].value.seq
    val userId = data.get("user_id").get.as[String]

    ProfileService.addJob(profileId, userId, data).map(res => response(res))
  }


  /**
   * Checks if user can actually make such request and then deletes it
   * @param profile_id
   * @param user_id
   * @return
   */
  def delete(profile_id: String, user_id: String) = AuthenticatedAction.async {

    val profileF:Future[JsObject] = ProfileService.getProfileViewById(profile_id, user_id)
    val deleteRequestF =  ProfileService.delete(profile_id,user_id)

    val deleteResults = for {
      f1 ← profileF
      f2 ← deleteIfAllowed(f1,user_id,profile_id)
    } yield f2

    deleteResults.map {res=>
      response(res)
    }

  }

  def deleteIfAllowed(profileJsObject:JsObject,userId:String, profileId:String):Future[JsObject] = {
    (profileJsObject \ "data" \ "can_delete").as[Boolean] match {
      case true =>ProfileService.delete(profileId,userId)
      case _ => Future.successful((BaseServiceO.error(RestStatusCodes.ERROR_DELETE_OBJ, "You can't delete this profile")))
    }

  }


  def popularWithoutCache(pos: Int, tags:String) = AuthenticatedAction.async {
    log.trace(s"popular, pos: $pos, tags: $tags")


    val list = ProfileController.convertToList(tags)

    log.debug("tags amount:" + list.size)


    list.size match {
      case 0 => errorAsyncResponse(RestStatusCodes.ERROR_OBJ_NOT_FOUND,"missing tag name")
      case 1 => {
        ProfileService.getPopularProfilesWithoutCache(pos,List(list(0))) map {
          result => successResponse(JsArray(result))
        }
      }
      case _ => {
        ProfileService.getPopularProfilesWithoutCache(pos,list) map {
          result => successResponse(JsArray(result))
        }

      }
    }

  }

  /**
   * approves profile creation form the external link
   * @param uid
   * @return
   */
  def approveProfile(uid: String) = LoggingAction.async {request=>
    log.trace(s"approveProfile, uid: $uid ")
    ProfileService.approvalStateChange(uid,ProfileState.Approved).map {
      res =>
        getStatus(res) match {
          case 0 => Ok(views.html.profileApprovalResponse(true,(res\ "data" \ "profileId") .as[String]))
          case _ => Ok(views.html.errorPage(getStatus(res), getErrorMessage(res)))
        }
    }

    //TODO: trigger mobile notification for the person who is creating a profile
  }



  def rejectProfile(uid: String) = LoggingAction.async {request=>
    log.trace(s"rejectProfile, uid: $uid ")

    ProfileService.approvalStateChange(uid,ProfileState.Rejected).map {
      res =>
        getStatus(res) match {
          case 0 => Ok(views.html.profileApprovalResponse(false,(res\ "data" \ "profileId") .as[String]))
          case _ => Ok(views.html.errorPage(getStatus(res), getErrorMessage(res)))
        }
    }
    //TODO: trigger mobile notification for the person who is creating a profile
  }
}
