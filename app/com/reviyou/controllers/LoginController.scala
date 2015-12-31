package com.reviyou.controllers

import com.reviyou.controllers.actions.{LoggingAction, AuthenticatedAction}
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future
import com.reviyou.common.dto.LoginRequest
import java.io.InputStream

//import reactivemongo.bson.{BSONValue, BSONObjectID}

import com.reviyou.models.UserModel
import com.reviyou.services._
import com.reviyou.common.{RestStatusCodes, CustomException}
import org.slf4j.LoggerFactory



/**
 * Login with social network, Logout, sign terms and conditions
 */
//TODO:todo:add services layer, don't call DAO from the controller directly!

object LoginController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)
  val imagesUrl = Play.application.configuration.getString("web.apiimage.user").get
  val ourDefaultImageUrl = Play.application.configuration.getString("image.default.url").get

  //helper class for the returning type
  class LoginResult(userId:String,termsSigned:Boolean, isDefault:Boolean, tags:Option[List[String]]) {
    def getUserId() = userId
    def isTermsSigned() = termsSigned

    def getJsonResponse():JsObject = {
      //full url for the images to rended against web server i e http://127.0.0.1:9000/api/1.0/rest/image/sashany@gmail.com_12222.png,special extention to differenciate a spacial default images in nginx config
      //val imgFullUrl = imagesUrl + imageName;
      val imgFullUrl =  isDefault match  {
        case true=>ourDefaultImageUrl
        case false=>imagesUrl + userId+".png"
      }

      Json.obj("userId" -> userId, "terms_signed" -> termsSigned, "user_image_url" -> imgFullUrl, "tags"->tags)
    }
  };


  def parseRequest(data: JsValue): LoginRequest = {
    val loginRequest: LoginRequest =
      data.validate[LoginRequest].map(request => request).recoverTotal{
        log.trace("request: " + data.toString())
        e => {
          throw CustomException.create(RestStatusCodes.REQUEST_PARSING_EXCEPTION, "Can not parse login request")
        }
      }
    loginRequest
  }

  /**
   * login with fb\tw\g+, create new record or update existing one(based on the uniquness of the email), update history of this user
   *
   * @return userId or error if can't login
   */
  def login = LoggingAction.async(parse.json) { request =>
    log.trace("login: "+ request.body)
    try {
      val loginRequest: LoginRequest = parseRequest(request.body)

      val validationStatusF: Future[Boolean] = AuthorizationService.realTokenValidation(loginRequest) //validate original token
      val userModelF :Future[Option[UserModel]] = UserDaoOld.findByEmail(loginRequest.email)
      val userImageF :Future[InputStream] = ImagesService.loadProfilePicture(loginRequest)


      userModelF onFailure  {
        case e: Exception =>
          errorResponse(RestStatusCodes.DEFAULT_CUSTOM_EXCEPTION, e.getMessage, "can't search user in our system by email")
      }
      //wait for all futures to complete asynchronously
      //val aggFut =
      val result = for {
        f1ValidationStatus <- validationStatusF
        f2FoundDataModel <- userModelF
        f3LoadedUserImage <-userImageF
        f4Result <-processValidUser(f1ValidationStatus,loginRequest,f2FoundDataModel,f3LoadedUserImage)
      } yield f4Result

      result onFailure {
        case e: Exception =>
          //todo: test this failure
          errorResponse(RestStatusCodes.DEFAULT_CUSTOM_EXCEPTION, e.getMessage, "can't create or update user")

      }

      result

    } catch {
      //TODO: get rid of this exception in the future, once all cases are covered
      case ex: CustomException => errorAsyncResponse(RestStatusCodes.DEFAULT_CUSTOM_EXCEPTION, ex.getMessage)
    }
  }



  def processValidUser(validationStatusF: Boolean, loginRequest: LoginRequest, userModelO :Option[UserModel],userImage:InputStream):Future[SimpleResult] = {
    log.trace(s"processValidUser");
    if(!validationStatusF) return errorAsyncResponse(RestStatusCodes.AUTHORIZATION_ERROR, "not a valid token");

    val loginResult:Future[LoginResult] = userModelO match {//whether user exist or not
      case Some(userModel) => UserService.updateUser(userModel, loginRequest, userImage);
      case None => UserService.createUser(loginRequest, userImage);
    }

    loginResult.map {res=>
      UserDaoOld.addLoginHistoryRecord(loginRequest, res.getUserId())
      successResponse(res.getJsonResponse())
    }
  }






  def logout (userId: String, loginToken: String) = LoggingAction.async {
    log.trace(s"logout, userId: $userId.")
    try {

      UserDaoOld.logoutUser(userId)//service
      UserDaoOld.addLogoutHistoryRecord(userId)
    } catch{
      case ex:CustomException=>errorAsyncResponse(RestStatusCodes.USER_UPDATE_ERROR, ex.getMessage)
    }
    successAsyncResponse(Json.obj())
  }

  /**
   * Mark user as marked terms and conditions(only required once), but we check every login if he signed already
   * @param user_id
   * @return
   */

  def signTerms (user_id: String) = AuthenticatedAction.async (parse.json) { request=>

    log.trace(s"signTerms, userId: $user_id.")

    val modifier = Json.obj("$set" -> Json.obj ("terms_signed" -> true ))
    try {
      UserDaoOld.updateUser(user_id, modifier)
    } catch{
      case ex:CustomException=>errorAsyncResponse(RestStatusCodes.USER_UPDATE_ERROR, ex.getMessage)
    }

    successAsyncResponse(Json.obj())
  }
}

