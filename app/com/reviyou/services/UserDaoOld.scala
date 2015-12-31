package com.reviyou.services

import com.reviyou.services.db.DBQueryBuilder

import scala.concurrent.Future
import com.reviyou.models.{Settings, UserModel, Profile}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api._
import reactivemongo.bson.BSONObjectID
import com.reviyou.common.CustomException
import scala.util.{ Failure, Success }
import org.joda.time.DateTime
import com.reviyou.common.dto.LoginRequest
import org.slf4j.LoggerFactory

/** 
 *  A data access object for profiles backed by a MongoDB collection
 *  @deprecated
 */
//todo: incorporation into the other UserDao class. Don't add more methods into this one.
@deprecated
object UserDaoOld {

  val log = LoggerFactory.getLogger(getClass)

  private def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")

  private def loginHistory: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users_history")


  /**
   * Find the profile by email.
   *
   * @return All of the profiles.
   */
  def findByEmail(email: String): Future[Option[UserModel]] = {
    log.trace("findbyemail " + email)
    val userModel = users.find(Json.obj("email" -> email))
      .one[UserModel]

    return userModel
  }



  def findByUserId(userId: String): Future[Option[UserModel]] = {
    log.trace(s"findByUserId  userId: ${userId}")
    users.find(DBQueryBuilder.id(userId)).one[UserModel]
  }



  //service
  //todo: make image not an option
  def updateExistingUserRecord(userId: String, tags:Option[List[String]],settings:Settings,loginRequest: LoginRequest) = {
    log.trace(s"updateExistingUserRecord, userId: $userId, tags: $tags")
    val modifier = loginRequest.existingUserModel(userId,tags,settings)
    updateUser(userId, modifier)
  }

  //service
  def logoutUser(userId: String) = {
    log.trace(s"logoutUser, userId: $userId.")

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "login_data" -> Json.obj(
          "token" -> "",
          "expiration_time" -> new DateTime().minusDays(10).getMillis //TODO: should be set to NOW, not -10 days
        )
      )
    )
    updateUser(userId, modifier)
  }

  //actual dao
  def updateUser(userId: String, updateData: JsObject) = {
    users.update(Json.obj("_id" -> Json.obj("$oid" -> userId)), updateData)
      .onComplete {
      case Failure(e) => throw e
      case Success(_) => Some
    }
  }


  /**
   * adding logout record to the history of user's events
   * @param userRequest
   * @param userId
   */
  def addLoginHistoryRecord(userRequest: LoginRequest, userId: String) {
    val id = BSONObjectID.generate.stringify
    log.trace(s"addLoginHistoryRecord, id: $id, userId: $userId")

    val loginHistoryRecord = Json.obj(
      "_id" -> Json.obj("$oid" -> id),
      "userId" -> userId,
      "action" -> "login",
      "login_time" -> new DateTime().getMillis,
      "token_type" -> userRequest.login_provider //, // getObject.value.get("login_provider"),
      //"device" -> userRequest.getObject.value.get("device"),
      //"fb_login_data" -> userRequest.facebook_login_metadata, //getObject.value.get("login_data")
      //"google_login_data" -> userRequest.google_login_metadata
    )

    loginHistory.insert(loginHistoryRecord).map(lastError =>
      if (!lastError.ok) {
        throw CustomException.create("cannot insert login history record")
      }
    )
  }

  /**
   * adding login record to the history of user's events
   * logout history record
   * @param userId
   */
  def addLogoutHistoryRecord(userId: String) {
    val id = BSONObjectID.generate.stringify
    log.trace(s"addLogoutHistoryRecord, id: $id, userId: $userId")
    val logoutHistoryRecord = Json.obj(
      "_id" -> Json.obj("$oid" -> id),
      "user_id" -> userId,
      "action" -> "logout",
      "logout_time" -> new DateTime().getMillis
    )

    loginHistory.insert(logoutHistoryRecord).map(lastError =>
      if (!lastError.ok) {
        throw CustomException.create("cannot insert logout history record")
      }
    )
  }

}