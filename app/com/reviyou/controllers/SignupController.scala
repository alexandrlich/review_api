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
import com.reviyou.services.{UserService, ImageDao, UserDaoOld}
import org.slf4j.LoggerFactory


/**
 * Create a new account, reset password
 */
object SignupController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)


  def parseSignUpRequest(data: JsValue): SignupRequest = {
    val signupRequest: SignupRequest =
      data.validate[SignupRequest].map(request => request).recoverTotal{
        e => {
          throw CustomException.create(RestStatusCodes.REQUEST_PARSING_EXCEPTION, "Can not parse signup request")
        }
      }
    signupRequest
  }

  /**
   * check if username is not taken, save it and send an email to confirm the change
   * @return
   */
  def signup = LoggingAction.async(parse.json) { request =>
    log.trace(s"signup: ${request.body}")
    try {
      val signupRequest: SignupRequest = parseSignUpRequest(request.body)

      UserService.signup(signupRequest) map {
        result => successResponse(result)
      }


    } catch {
      //TODO: get rid of this exception in the future, once all cases are covered
      case ex: CustomException => errorAsyncResponse(RestStatusCodes.DEFAULT_CUSTOM_EXCEPTION, ex.getMessage)
    }
  }

  def resetPassword = LoggingAction.async(parse.json) { request =>
    log.trace(s"resetPassword: ${request.body}")
    try {

        val cObj = request.body.as[JsObject].value
        val email = cObj.get("email").get.as[String]
        val password = cObj.get("password").get.as[String]

        UserService.resetPassword(email,password) map {
          result => successResponse(result)
        }


    } catch {
      //TODO: get rid of this exception in the future, once all cases are covered
      case ex: CustomException => errorAsyncResponse(RestStatusCodes.DEFAULT_CUSTOM_EXCEPTION, ex.getMessage)
    }
  }

  /**
   * registration confirmation
   * @param uid
   * @return
   */
  def accountCreationApprovalClick(uid: String)  = LoggingAction.async { request =>
    log.trace(s"signupApprove, uid: $uid ")

    UserService.accountCreationApproved(uid).map {
      res =>
        getStatus(res) match {
          case 0 => Ok(views.html.accountChangeApprovalResponse())
          case _ => Ok(views.html.errorPage(getStatus(res), getErrorMessage(res)))
        }
    }
  }

  /**
   * password reset confirmation
   * @param uid
   * @return
   */
  def accountPassResetApprovalClick(uid: String)  = LoggingAction.async { request =>
    log.trace(s"accountPassResetApprovalClick, uid: $uid ")

    UserService.accountPassResetApproved(uid).map {
      res =>
        getStatus(res) match {
          case 0 => Ok(views.html.accountChangeApprovalResponse())
          case _ => Ok(views.html.errorPage(getStatus(res), getErrorMessage(res)))
        }
    }
  }


}

