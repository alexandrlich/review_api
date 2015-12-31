package com.reviyou.controllers

import org.joda.time.DateTime
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.{Json, JsObject}
import play.api.test._
import play.api.test.Helpers._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json.toJsFieldJsValueWrapper

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
//TODO: old test doesn't work, should test com.reviyou.services layer
@RunWith(classOf[JUnitRunner])
class UserDaoSpec extends Specification {

  "The 'Hello world' string" should {
    "contain 11 characters" in {
      "Hello world" must have size(11)
    }
    "start with 'Hello'" in {
      "Hello world" must startWith("Hello")
    }
    "end with 'world'" in {
      "Hello world" must endWith("world")
    }
  }


  /*

  "Application" should {

    def test

    def initDB(colection: JSONCollection, prefix: String, expireDate: Long, id: String) = {

      val json = Json.obj(
        "_id" -> Json.obj("$oid" -> id),
        "first_name" -> "",
        "last_name" -> "",
        "email" -> (prefix + "@gmail.com"),
        "login_data" -> Json.obj(
          "token" -> prefix,
          "expiration_time" -> expireDate
        ),
        "bookmarks" ->  Json.arr(),
        "user_profile_image" -> ""
      )

      colection.insert(json).map(lastError=>lastError)

    }

    "new user login" in new WithApplication {

      def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
      val prefix = "new_user_login_test" +  new java.util.Date().getTime
      val result = route(FakeRequest(POST, "/login").withJsonBody(
        Json.obj(
          "login_provider" -> "facebook",
          "login_data" -> Json.obj(

            "accessToken" -> "test token lalala",
            "expirationTime" -> new DateTime().plusDays(1).getMillis,
            "expiresIn" -> "",
            "fbUserId" -> "string",
            "email" -> (prefix+"@gmail.com")
          ),
          "device" -> Json.obj(
            "model" -> "test_model",
            "uuid" -> "test_uuid",
            "version" -> "test_version",
            "platform" -> "test_platform"
          )

        )
      )).get
      status(result) must equalTo(OK)
      val resultJson: JsObject = contentAsJson(result).as[JsObject]
      resultJson should_!= null
      resultJson.value.size should_== 1
      val id = resultJson.value.get("userId").get
      id should_!= null

      val cursor: Cursor[JsObject] = users
        .find(Json.obj("_id" ->  Json.obj("$oid" -> id))).cursor[JsObject]

      val futureUsersJsonArray = cursor.collect[List]() map {
        userList => userList
      }

      futureUsersJsonArray.map { users =>

        users should_!= null
        users.length should_== 1
        (prefix+"@gmail.com") shouldEqual users(0).value.get("email").get.as[String]
      }
    }


    "exist user login with valid token" in new WithApplication {
      def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
      val prefix = "valid_user_login_test" +  new java.util.Date().getTime
      val _id = BSONObjectID.generate.stringify
      initDB(users, prefix, new DateTime().plusDays(1).getMillis, _id)


      val result = route(FakeRequest(POST, "/login").withJsonBody(
        Json.obj(
          "login_provider" -> "facebook",
          "login_data" -> Json.obj(

            "accessToken" -> prefix,
            "expirationTime" -> new DateTime().plusDays(1).getMillis,
            "expiresIn" -> "",
            "fbUserId" -> "string",
            "email" -> (prefix+"@gmail.com")
          ),
          "device" -> Json.obj(
            "model" -> "test_model",
            "uuid" -> "test_uuid",
            "version" -> "test_version",
            "platform" -> "test_platform"
          )

        )
      )).get
      status(result) must equalTo(OK)
      val resultJson: JsObject = contentAsJson(result).as[JsObject]
      resultJson should_!= null
      resultJson.value.size should_== 1
      val id = resultJson.value.get("userId").get.as[String]
      id should_!= null
      id shouldEqual _id
    }


    "exist user login with expired token" in new WithApplication {
      def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
      val prefix = "expired_user_login_test" +  new java.util.Date().getTime
      val id = BSONObjectID.generate.stringify
      initDB(users, prefix, new DateTime().minusDays(10).getMillis, id)

      val result = route(FakeRequest(POST, "/login").withJsonBody(
        Json.obj(
          "login_provider" -> "facebook",
          "login_data" -> Json.obj(

            "accessToken" -> prefix,
            "expirationTime" -> new DateTime().plusDays(1).getMillis,
            "expiresIn" -> "",
            "fbUserId" -> "string",
            "email" -> (prefix+"@gmail.com")
          ),
          "device" -> Json.obj(
            "model" -> "test_model",
            "uuid" -> "test_uuid",
            "version" -> "test_version",
            "platform" -> "test_platform"
          )

        )
      )).get

      status(result) must equalTo(INTERNAL_SERVER_ERROR)
    }


  }
  */
}
