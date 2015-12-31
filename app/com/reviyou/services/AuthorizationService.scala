package com.reviyou.services

import com.reviyou.common.dto.{LoginRequest, SignupRequest}
import com.reviyou.controllers.LoginController.LoginResult
import com.reviyou.models.ProfileState
import com.reviyou.services.dao.{UserCredencialsDao, UserDao}
import com.reviyou.services.db.DBQueryBuilder
import com.reviyou.services.exceptions.ServiceException
import dispatch._
import org.json4s
import org.mindrot.jbcrypt.BCrypt
import play.api.Play
import play.api.libs.json.{JsValue, Json}
import com.reviyou.common.RestStatusCodes._
import play.api.libs.concurrent.Execution.Implicits._
import com.reviyou.models._
import scala.concurrent.Future
import org.slf4j.LoggerFactory


import play.api.libs.json.JsObject
import com.reviyou.common.{CustomException, Utils}

import play.api.Play.current

import scala.util.{Success, Failure}


object AuthorizationService extends BaseService {

  val log = LoggerFactory.getLogger(getClass)


  val appId = Play.application.configuration.getString("reviyou.appicationId").get
  val facebookUrl = "https://graph.facebook.com/oauth/access_token_info?"
  val fbClientId = Play.application.configuration.getString("facebook.clientId").get
  val googleClientId = Play.application.configuration.getString("google.clientId").get

  //disable 401 verification on test environment
  val bypassRealValidation = Play.application.configuration.getBoolean("user.bypass.realValidation").getOrElse(false)
  val bypassLocalValidation:Boolean = Play.application.configuration.getBoolean("user.bypass.localValidation").getOrElse(false)


  val googleUrl = "https://www.googleapis.com/oauth2/v1/tokeninfo?"

  val reviyouUrl = Play.application.configuration.getString("web.apirest").get + "/tokeninfo?"

  // Random generator
  val random = new scala.util.Random

  //todo: in the future add expiration date to the account specific tokens
  /**
   * public service to check token is in our system and active(against our database)
   * @param token
   * @return
   */
  def checkTokenActive(token: String): Future[JsObject] = {
    log.trace(s"verifyToken, token: $token");
    UserCredencialsDao.findOne(Json.obj("token"->token,"account_approved"->true)) flatMap {
      case Some(uModel) =>
        Future(BaseServiceO.success(Json.obj("valid"->true)))

      case None =>
        Future(BaseServiceO.error(AUTHORIZATION_ERROR,"Token is invalid"))

    }
  }

  /**
   * checks authorization is made from the allowed device
   * checks username and password match,
   * generates auth_token for this account
   * @param email
   * @param password
   * @param requestAppId
   * @return token, email first and last name
   */
  def getToken(email:String, password:String,requestAppId:String):Future[JsObject] = {
    log.trace(s"getToken, email: $email, appId: $requestAppId")

    appId ==requestAppId match {
      case false => Future(BaseServiceO.error(AUTHORIZATION_ERROR, "invalid application id"))
      case _ => {
        UserCredencialsDao.findOne(Json.obj("email" -> email.toLowerCase, "account_approved" -> true)) flatMap {
          case Some(uModel) => {
            import org.mindrot.jbcrypt._
            BCrypt.checkpw(password, uModel.password.get) match {
              case false => Future(BaseServiceO.error(ERROR_SIGN_IN_FAILED, "Password is invalid."))
              case _ => {
                val token = generateToken()
                //save token in our system and TODO: expire in XXX hours
                UserCredencialsDao.update(uModel.getId(),DBQueryBuilder.set(Json.obj("auth_token"->token))) map {
                  case Right(b) => BaseServiceO.success(
                    Json.obj("email" -> email,
                      "auth_token" -> token,
                      "first_name" -> uModel.first_name,
                      "last_name" -> uModel.last_name))
                  case Left(e) => BaseServiceO.error(ERROR_UPDATE_OBJ, e.message)
                }

              }
            }
          }
          case _ => Future(BaseServiceO.error(ERROR_SIGN_IN_FAILED, "No active account with such email and password found."))

        }
      }
    }
  }

  /**
   * generates security token
   * @return
   */
  def generateToken():String = {
    randomString("abcdefghijklmnopqrstuvwxyz0123456789")(20)
  }

  // Generate a random string of length n from the given alphabet
  def randomString(alphabet: String)(n: Int): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString


  /**
   *
   * Private call after authorization to validate against authorization provider at least once to make sure token in real
   * After that we'll only validate against our database.
   *
   * Validate account token once after login, see if validation should be bypassed for internal usage or not

   * check with fb\google\reviyou it's a valid token to proceed
   * @param userRequest
   * @return
   *
   * @see https://developers.google.com/apis-explorer/#p/oauth2/v2/oauth2.tokeninfo and
   *      https://developers.facebook.com/docs/facebook-login/access-tokens
   */
  def realTokenValidation(userRequest: LoginRequest) :Future[Boolean] = {
    userRequest.email match {
      case ryEmail if((ryEmail endsWith "@reviyou.com") && bypassRealValidation)=> {
        log.warn(s"validation with 3rd party provider is bypassed for the email address ${ryEmail}")
        Future.successful(true)
      }
      case _ => {
        userRequest.login_provider match {
          case LoginProvider.Facebook =>
            val fbUrl = s"${facebookUrl}client_id=$fbClientId&access_token=${userRequest.access_token}"
            validateGoogleOrFacebook(fbUrl)
          case LoginProvider.Google =>
            val ggUrl = s"${googleUrl}access_token=${userRequest.access_token}"
            validateGoogleOrFacebook(ggUrl)
          case LoginProvider.Twitter =>
            //no easy way to validate yet(since twitter desn't use oauth 2.0)so leave it for later
            return Future(true);
          case LoginProvider.Reviyou =>
            //todo: validation against our db, for reviyou it's no different so we can skip it for now.
            return Future(true);
          //throw CustomException.create("twitter is not 100% supported yet")

          case _ =>throw CustomException.create("unsupported login provider")
        }
      }
    }
  }

  /**
   * validate every api call to make sure token matchthe user
   * @param userId
   * @param token
   * @return
   */
  def tokenMatchValidation(userId:String, token:String) :Future[Boolean] = {
    //temporarily check for dev purposes
    bypassLocalValidation match {
      case true => Future(true)
      case _ => {
        UserDao.findById(userId) map {
          case Some(user) =>
            (user.getUserToken == token) && !user.isExpired
          case None => false
        }
      }

    }
  }

    /**
   *
   * @see https://developers.google.com/apis-explorer/#p/oauth2/v2/oauth2.tokeninfo and
   *      https://developers.facebook.com/docs/facebook-login/access-tokens
   * @param platformUrl
   * @return
   */
  def validateGoogleOrFacebook(platformUrl: String) :Future[Boolean] ={
    log.trace("validateGoogleOrFacebook")
    // val ggUrl = googleUrl + "access_token=" + request.getUserToken
    //println("+++++++++++++++++++++++++++++++++++++++++4" + platformUrl)
    val ggResponse: Future[Option[json4s.JValue]] = Http(url(platformUrl) OK dispatch.as.json4s.Json).option
    ggResponse.map { jsonO=>
      val isValid = jsonO match {
        case Some(json) => {
          log.info(json.toString)
          true
        }
        case None => {
          log.error(s" can't authorize url ${platformUrl}, didn't retrieve validation response")
          false
        }
      }
      isValid
    }
  }

}
