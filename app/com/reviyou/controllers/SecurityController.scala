package com.reviyou.controllers

import java.io.InputStream

import com.reviyou.common.dto.{SignupRequest, LoginRequest}
import com.reviyou.controllers.actions.{AuthenticatedAction, LoggingAction}
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

//import reactivemongo.bson.{BSONValue, BSONObjectID}

import com.reviyou.common.{CustomException, RestStatusCodes}
import com.reviyou.models.UserModel
import com.reviyou.services._
import org.slf4j.LoggerFactory


/**
 * User Authorization, token verification
 */
object SecurityController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)




  /**
   * checks user exist and appId matches, respond with username,email and generated auth-token
   *
   */
  def getToken  = LoggingAction.async(parse.json)  { request =>
    log.trace(s"authorize")
    val cObj = request.body.as[JsObject].value
    val email = cObj.get("email").get.as[String]
    val password = cObj.get("password").get.as[String]
    val appId = cObj.get("reviyouAppId").get.as[String]

    AuthorizationService.getToken(email, password, appId).map {
      res => response(res)

    }
  }

  //check against our db
  def checkTokenActive(token: String) = LoggingAction.async { request =>
    log.trace(s"verify")
    AuthorizationService.checkTokenActive(token).map {
      res => response(res)
    }
  }
}

