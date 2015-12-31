package com.reviyou.controllers

import com.reviyou.models.UserModel
import org.joda.time.DateTime
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import com.reviyou.utils.MongoDBTestUtils._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.JsObject
import org.scalatest._
import Matchers._

//TODO: old test doesn't work, should test com.reviyou.services layer
@RunWith(classOf[JUnitRunner])
class LogoutSpec extends Specification {

  val token = "sometoken"

    def initDB(colection: JSONCollection, prefix: String, expireDate: Long, id: String) = {

      val json = Json.obj(
        "_id" -> Json.obj("$oid" -> id),
        "first_name" -> "f",
        "last_name" -> "l",
        "email" -> (prefix + "@gmail.com"),
        "login_data" -> Json.obj(
          "token" -> token,
          "expiration_time" -> expireDate
        ),
        "bookmarks1" ->  Json.arr(),
        "user_profile_image" -> ""
      )
      colection.insert(json).map(lastError=>lastError)
    }


    "USER LOGOUT"  in withMongoDb  {implicit app =>
      def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
      val prefix = "valid_user_login_test" +  new java.util.Date().getTime
      val userId = BSONObjectID.generate.stringify
      println("logoutuserId:" + userId)
      initDB(users, prefix, new DateTime().plusDays(1).getMillis, userId)

      val response = route(FakeRequest(GET, "/api/1.0/rest/logout?user_id=" + userId + "&user_token=" + token)).get

      val cursor: Cursor[JsObject] = users
        .find(Json.obj("_id" ->  Json.obj("$oid" -> userId))).cursor[JsObject]

      val futureUsersJsonArray = cursor.collect[List]() map {
        userList => userList
      }

      futureUsersJsonArray.onComplete { users =>
        users should_!= null
        users.get.length should_== 1
        val userModel = users.get.head.as[UserModel]
        userModel.isExpired ===true
        println("found after logout: " + userModel.toString)
        (userModel.getUserToken.isEmpty) ===true
      }

      status(response) must equalTo(OK)
      val jsonResponse: JsObject = contentAsJson(response).as[JsObject]
      println("logout resultJson1 " +  jsonResponse.toString())
      jsonResponse \ "status" === JsNumber(0)

    }
}
