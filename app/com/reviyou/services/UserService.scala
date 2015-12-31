package com.reviyou.services

import com.reviyou.common.dto.{LoginRequest, SignupRequest}
import com.reviyou.controllers.LoginController.LoginResult
import com.reviyou.models.ProfileState
import com.reviyou.services.dao.{UserCredencialsDao, UserDao}
import com.reviyou.services.exceptions.ServiceException
import play.api.Play
import play.api.cache.Cache
import play.api.libs.json.{JsValue, Json}
import com.reviyou.common.Utils._
import com.reviyou.common.RestStatusCodes._
import play.api.libs.concurrent.Execution.Implicits._
import com.reviyou.models.dto._
import com.reviyou.models._
import com.reviyou.models.Profile._
import scala.collection.Map
import scala.concurrent.Future
import com.reviyou.services.db.DBQueryBuilder
import reactivemongo.bson.BSONObjectID
import org.slf4j.LoggerFactory
import com.reviyou.models.Job
import com.reviyou.models.ProfileSkill
import play.api.libs.json.JsObject
import com.reviyou.common.{CustomException, Utils}

import play.api.Play.current

import scala.util.{Success, Failure}


object UserService extends BaseService {

  val log = LoggerFactory.getLogger(getClass)


  /**
   * request to create a new account
   * @param signupRequest
   * @return
   */
  def signup(signupRequest: SignupRequest): Future[JsObject] = {
    log.trace(s"signup, email: ${signupRequest.email}");
    UserCredencialsDao.findOne(Json.obj("email"->signupRequest.email.toLowerCase,"account_approved"->true)) flatMap {
      case Some(uModel) =>
        Future(BaseServiceO.error(ERROR_USER_ACCOUNT_EXISTS,"Reviyou account exist already, try to sign up or reset a password."))

      case None => {
        val confirmationUid = java.util.UUID.randomUUID.toString
        UserCredencialsDao.insert(UserCredencials.createAccount(signupRequest,confirmationUid)) map {
          case Right(b) => EmailService.sendAccountApprovalRequest(signupRequest, confirmationUid)
          case Left(e) => BaseServiceO.error(ERROR_INSERT_OBJ_TO_DB, e.message)
        }
      }

    }
  }

  /**
   * save request to reset a password and mail a confirmation link
   * @param email user's email to confirm a change
   * @param newPassword
   * @return
   */
  def resetPassword(email: String, newPassword: String): Future[JsObject] = {
    log.trace(s"resetPassword, email: $email");
    UserCredencialsDao.findOne(Json.obj("email"->email.toLowerCase,"account_approved"->true)) flatMap {
      case Some(uModel) =>

        val confirmationUid = java.util.UUID.randomUUID.toString
        UserCredencialsDao.update(uModel.getId(),DBQueryBuilder.set(uModel.resetPasswordRequest(newPassword, confirmationUid))) map {
          case Right(b) => EmailService.sendAccountPassResetRequest(email, confirmationUid)
          case Left(e) => BaseServiceO.error(ERROR_UPDATE_OBJ, e.message)
        }
      case _ =>Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND,"No account with such email exist yet - please SignUp"))


    }
  }


  /**
   * Approving sign up request when user confirms it
   * @param uid - unique identifier for user to confirm account creation request from his email
   * @return
   */
  def accountCreationApproved(uid:String):Future[JsObject] = {
    log.trace(s"accountCreationApproved, uid: $uid");
    UserCredencialsDao.findOne(Json.obj("conf_uid" -> uid, "account_approved" -> false)) flatMap {
      case Some(account) => updateAccountCreds(account.approvedAccount())
      case _ => Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, "There is nothing to approve, SignUp first please."))

    }
  }

  def updateAccountCreds(userCreds: UserCredencials) = {
    UserCredencialsDao.update(userCreds.getId, DBQueryBuilder.set(userCreds)) map {
      case Right(b) => BaseServiceO.success()
      case Left(e) => BaseServiceO.error(ERROR_APPROVE_PROFILE, e.message)
    }
  }

  /**
   * Approving password reset when user confirms
   * @param uid - unique identifier for user to confirm password reset request from his email
   * @return
   */
  def accountPassResetApproved(uid:String):Future[JsObject] = {
    log.trace(s"signupApprove, uid: $uid");
    UserCredencialsDao.findOne(Json.obj("conf_uid" -> uid, "account_approved" -> true, "pass_reset_approved" -> false)) flatMap {
      case Some(account) => updateAccountCreds(account.approvedPassReset())
      case _ => Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, "There is nothing to approve, SignUp first if you don't have an account and reset your password if you lost it."))

    }
  }





  def createUser(loginRequest: LoginRequest,fileIO: java.io.InputStream) : Future[LoginResult] = {
    log.trace(s"createUser, email: ${loginRequest.email}");

    UserDao.insert(loginRequest.createUserModel()) flatMap {
      case Left(left) =>
        throw CustomException.create("Cannot create a new user:" + left.message)
      case Right(right) => {
        DevicesPushIntegrationService.associateUser(right.getUserId,loginRequest.getUUID)

        ImageDao.doUploadProfileImage(fileIO, right.getUserId).map { imageNme =>
          new LoginResult(right.getUserId, false, ImagesService.isDefaultPicture(loginRequest),None)
        }.recover {
          case e => {
            log.error(e.toString)
            new LoginResult(right.getUserId, false, ImagesService.isDefaultPicture(loginRequest),None)
          }
        }
      }
    }
  }

    /**
     * update user record (with image name) and image itself(remove an old one and add a new one)
     * @param userModel
     * @param loginRequest
     * @param fileIO
     * @return (userId,termsSigned,imageName) triple
     */
    def updateUser(userModel: UserModel,  loginRequest: LoginRequest, fileIO: java.io.InputStream) : Future[LoginResult] =  {
      log.trace(s"updateUser, email: ${loginRequest.email}");
      //remove an old image
      log.debug(s"file ${userModel.getUserImageName} is about to be removed(if exist)")
      ImageDao.removeFromWebdav(userModel.getUserImageName)

      UserDaoOld.updateExistingUserRecord(userModel.getUserId,userModel.tags,userModel.settings, loginRequest)
      DevicesPushIntegrationService.associateUser(userModel.getUserId,loginRequest.getUUID)


        ImageDao.doRemoveProfileImage(userModel.getUserImageName).map {isDeleted=>
        log.debug(s"isDeleted $isDeleted")
        ImageDao.doUploadProfileImage(fileIO, userModel.getUserId)
      }
      Future(new LoginResult(userModel.getUserId, true, ImagesService.isDefaultPicture(loginRequest),userModel.tags))
    }


  /**
   * adds or removes user's interest from his pferecenses
   * @param user_id
   * @param tag_name
   * @param addFlag true means add, false - remove
   * @return
   */
  def addRemoveTag(user_id: String, tag_name: String, addFlag:Boolean): Future[JsObject] = {
    log.trace(s"addRemoveTag : $tag_name user_id:  $user_id")

    com.reviyou.services.dao.UserDao.findById(user_id).flatMap {
      case Some(user) => {

        val f = addFlag match {
          case true =>  //UserDao.addToSet(user_id, UserDao.tag_name, tag_name)
            UserDao.update(user_id,DBQueryBuilder.addToSet(UserDao.tag_name_field, tag_name))
          case false =>  UserDao.pull(user_id, UserDao.tag_name_field, tag_name)
        }

        f  map {
          case Right(b) => BaseServiceO.success()
          case Left(e) => BaseServiceO.error(ERROR_UPDATE_OBJ, e.message)
        }

      }
      case _ => Future(BaseServiceO.error(ERROR_USR_NOT_FOUND, s"User with id = $user_id has not been found."))
    }
  }

  def updateSoundSettings(user_id:String, settingKey:String, value:Boolean): Future[JsObject] = {


    UserDao.update(user_id, DBQueryBuilder.set(Json.obj(("settings."+settingKey) ->value))) map {
      case Right(b) => BaseServiceO.success()
      case Left(e) => BaseServiceO.error(ERROR_UPDATE_OBJ, e.message)
    }
  }



}
