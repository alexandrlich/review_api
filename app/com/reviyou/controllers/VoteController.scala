package com.reviyou.controllers

import com.reviyou.common.{CustomException, RestStatusCodes}
import com.reviyou.controllers.LoginController._
import com.reviyou.controllers.actions.AuthenticatedAction
import com.reviyou.models.dto.VoteDto
import com.reviyou.services.ProfileService._
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsArray, JsObject, Json}
import com.reviyou.services.VoteService

import scala.concurrent.Future

/**
 * Created by eugenezhgirov on 7/24/14.
 */
object VoteController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)

  def generalVote(profileId: String) = AuthenticatedAction.async(parse.json) {
    request =>
      try {
        //basic validation params are passed correctly to avoid junk in db
        if(profileId.equalsIgnoreCase("undefined") ||
          profileId.isEmpty ) {
          throw CustomException.create("profileId is not defined")
        }

        val obj = request.body.as[JsObject]
        val userId = obj.value.get("user_id").get.as[String]
        val voteVal = obj.value.get("general_vote").get.as[Int]
        val isRevote = obj.value.get("is_revote").get.as[Boolean]


        VoteService.storeVote(profileId, userId, voteVal, isRevote) map {res=>
          //case (code, voteDto) =>
          response(res)
          //case (code, voteDto) => response(code, voteDto.as[JsObject])
          //case voteDto => successResponse(voteDto.as[VoteDto])
          //case _ => errorResponse(RestStatusCodes.DEFAULT_CUSTOM_EXCEPTION, "Unknown exception")
        }
      } catch{
        case ex:CustomException=>errorAsyncResponse(RestStatusCodes.REQUEST_PARSING_EXCEPTION, ex.getMessage)
      }

  }

  def skillVote(profileId: String, skillId: String) = AuthenticatedAction.async(parse.json) {
    request =>
      try {
        log.trace(s"skillVote, profileId: $profileId, skillId: $skillId")

        val obj = request.body.as[JsObject]
        val userId = obj.value.get("user_id").get.as[String]
        val voteVal = obj.value.get("vote_value").get.as[Int]
        val isRevote = obj.value.get("is_revote").get.as[Boolean]

        //basic validation params are passed correctly to avoid junk in db
        if(skillId.equalsIgnoreCase("undefined") ||
          skillId.isEmpty ||
          profileId.equalsIgnoreCase("undefined") ||
          profileId.isEmpty ) {
          throw CustomException.create("skillId or profileId is not defined")
        }


        VoteService.storeVote(profileId, userId, voteVal,isRevote, Some(skillId)) map {res=>
          response(res)
        }

      } catch{
        case ex:CustomException=>errorAsyncResponse(RestStatusCodes.REQUEST_PARSING_EXCEPTION, ex.getMessage)
      }
  }

  def getVotedProfiles(user_id: String, offset: Int) = AuthenticatedAction.async {request=>
    log.trace(s"getVotedProfiles, user_id: $user_id, offset: $offset")

    VoteService.getVotedProfiles(user_id, offset).flatMap { fList =>
      fList.map(list => successResponse(JsArray(list)))
    }


  }

  def hideVotedProfile(profile_id:String) = AuthenticatedAction.async(parse.json) { request =>
    val userId = request.body.as[JsObject].value.get("user_id").get.as[String]
    log.trace(s"hideVotedProfile, user_id: $userId, voted_profile_id: $profile_id")
    VoteService.hideVotedProfile(profile_id, userId).map(res => response(res))

  }

}
