package com.reviyou.controllers

import org.joda.time.DateTime
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import com.reviyou.utils.MongoDBTestUtils._

import scala._

import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber



/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
//TODO: old test doesn't work, should test com.reviyou.services layer
@RunWith(classOf[JUnitRunner])
class FbLoginSpec extends Specification {

  val testValidTokenEndToUse="alex_fake_token_to_allow_junits_execution"

      def initDB(collection: JSONCollection, email: String, token: String, expireDate: Long, userId: String) = {
       // println(s"initDB, prefix: ${prefix}, userId: ${userId}")

        val jsonGeneratedUser = Json.obj(
          "_id" -> Json.obj("$oid" -> userId),
          "first_name" -> "firstName",
          "last_name" -> "lastName",
          "email" -> (email+"@reviyou.com"),
          "login_provider" -> "facebook",
          "token" -> token,

          "fb_login_data" -> Json.obj(
            //"username" -> "fbusername",
            //"expirationTime" -> expireDate,
            "expiresIn" ->"expiresInFb",
            "fbUserId" -> "fbUserId23",
            "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
            "is_default_image"->false

          ),
          "bookmarks" ->  Json.arr(),
          "user_profile_image" -> ""
        )

         collection.insert(jsonGeneratedUser).map(lastError=>lastError)
      }

    "new user fb login failed"  in withMongoDb  {implicit app =>

      //def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
      val prefix = "new_user_login_test" +  new java.util.Date().getTime
      val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
        Json.obj(
          "login_provider" -> "facebook",
          "access_token" -> "fake-token",       // would not pass with this token
          "email" -> (prefix+"@gmail.com"),
          "gender" -> "male",
          "first_name" -> "first_name",
          "last_name" -> "last_name",
          "gender" -> "male",

          "facebook_login_metadata" -> Json.obj(
            //"expirationTime" -> new DateTime().plusDays(1).getMillis,
            "expiresIn" -> "test123",
           // "username" -> "username1",
            "fbUserId" -> "string",
            "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
            "is_default_image"->false

          )//,

/*
          "google_login_metadata"-> Json.obj(
            "expiresIn" -> new DateTime().plusDays(1).getMillis,
            "name" -> "name1",
            "displayName" -> "displayName1",
            "idToken" -> "11",
            "tokenType" -> "tokenType1",
            "refreshToken" ->"refreshToken1",
            "googleUserId" -> "googleUserId1"
        )*/

      ))).get

        /* todo: add device
        "device" -> Json.obj(
            "model" -> "test_model",
            "uuid" -> "test_uuid",
            "version" -> "test_version",
            "platform" -> "test_platform"
          )
        */

      status(response) must equalTo(OK)
      val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
      println("jsonResponse: " + jsonResponse.toString())

      jsonResponse \ "status" === JsNumber(401)// since we can't pass a valid token - nothing else to test yet
    }



    "new user login succeeded"  in withMongoDb  {implicit app =>

      def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
      val prefix = "new_user_login_test" +  new java.util.Date().getTime
      val emailDomain = "@reviyou.com"

      val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
        Json.obj(
      "login_provider" -> "facebook",
      "access_token" -> ("token1" + testValidTokenEndToUse),
      "email" -> (prefix + emailDomain),
      "gender" -> "male",
      "first_name" -> "first_name",
      "last_name" -> "last_name",
      "gender" -> "male",

      "facebook_login_metadata" -> Json.obj(
        //"expirationTime" -> new DateTime().plusDays(1).getMillis,
        "expiresIn" -> "test123",
        //"username" -> "username1",
        "fbUserId" -> "string",
        "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
        "is_default_image"->false

      )
        )
      )).get
      status(response) must equalTo(OK)
      val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
      println("jsonResponse3: " + jsonResponse.toString())

      jsonResponse \ "status" === JsNumber(0)
      val jsonData :JsValue  = jsonResponse \ "data"

      jsonData \ "userId"  should_!= null
      (jsonData \ "isNewUser").toString() ==="true"
    }

  "new user login succeeded and comes back(new token)"  in withMongoDb  {implicit app =>

    def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
    val prefix = "repeated_user_login_test" +  new java.util.Date().getTime
    val emailDomain = "@reviyou.com"

    val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
      Json.obj(
        "login_provider" -> "facebook",
        "access_token" -> ("token1" + testValidTokenEndToUse),
        "email" -> (prefix + emailDomain),
        "gender" -> "male",
        "first_name" -> "first_name",
        "last_name" -> "last_name",
        "gender" -> "male",

        "facebook_login_metadata" -> Json.obj(
          //"expirationTime" -> new DateTime().plusDays(1).getMillis,
          "expiresIn" -> "test123",
          //"username" -> "username1",
          "fbUserId" -> "string",
        "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
        "is_default_image"->false
        )
      )
    )).get
    status(response) must equalTo(OK)
    val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
    println("jsonResponse3: " + jsonResponse.toString())

    jsonResponse \ "status" === JsNumber(0)
    val jsonData :JsValue  = jsonResponse \ "data"

    jsonData \ "userId"  should_!= null
    (jsonData \ "isNewUser").toString() ==="true"


    val response2 = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
      Json.obj(
        "login_provider" -> "facebook",
        "access_token" -> ("token2" + testValidTokenEndToUse),
        "email" -> (prefix + emailDomain),
        "gender" -> "male",
        "first_name" -> "first_name",
        "last_name" -> "last_name",
        "gender" -> "male",

        "facebook_login_metadata" -> Json.obj(
          //"expirationTime" -> new DateTime().plusDays(1).getMillis,
          "expiresIn" -> "test123",
          //"username" -> "username1",
          "fbUserId" -> "string",
          "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
          "is_default_image"->false

        )
      )
    )).get
    status(response2) must equalTo(OK)
    val jsonResponse2 :JsObject  = contentAsJson(response2).as[JsObject]
    println("jsonResponse4: " + jsonResponse2.toString())

    jsonResponse2 \ "status" === JsNumber(0)


  }



  "existing user login with new valid token"  in withMongoDb  {implicit app =>

    def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
    val oldToken = "valid_user_login_test" +  new java.util.Date().getTime
    val userId = BSONObjectID.generate.stringify //we use id only because we want to make sure the same record is returned
  val emailDomain = "@reviyou.com"
    val email="alex2221"

    println("insert fake existing user with id:" + userId + " and email:" + (email + emailDomain))
    initDB(users, email, oldToken, new DateTime().plusDays(1).getMillis, userId)

    val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
      Json.obj(
        "login_provider" -> "facebook",
        "access_token" -> ("token_new" + testValidTokenEndToUse),
        "email" -> (email + emailDomain),
        "gender" -> "male",
        "first_name" -> "first_name",
        "last_name" -> "last_name",
        "gender" -> "male",

        "facebook_login_metadata" -> Json.obj(
          //"expirationTime" -> new DateTime().plusDays(1).getMillis,
          "expiresIn" -> "",
          //"username" -> "username1",
          "fbUserId" -> "string345",
          "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
          "is_default_image"->false

        )
      )
    )).get
    status(response) must equalTo(OK)
    val resultJson: JsObject = contentAsJson(response).as[JsObject]
    println("resultJson7 " +  resultJson.toString())

    val jsonData :JsValue  = resultJson \ "data"

    (jsonData \ "userId").as[String]  === userId
    (jsonData \ "isNewUser").as[Boolean]  ===false

    //###########################################################################
    //check user can do somethign with a new token and pass authorization filter

    println("test updated token works with secured requests")


    val responseTest = route(FakeRequest
      //+ ("token_new" + testValidTokenEndToUse)
      //            Secured?user_id=22222&"
      (GET, "/api/1.0/rest/testServiceGetSecured?user_id="+userId + "&user_token=" +
        ("token_new" + testValidTokenEndToUse)+"&foo=fooTest&bar=barTest")).get


    val resultJsonTest: JsObject = contentAsJson(responseTest).as[JsObject]
    println("resultJson8 " +  resultJsonTest.toString())
    status(responseTest) must equalTo(OK)
    resultJsonTest \ "status" === JsNumber(0)

  }


      "existing user login with not allowed domain to pass fb validation" in new WithApplication {
        def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
        val oldToken = "expired_user_login_test" +  new java.util.Date().getTime
        val userId = BSONObjectID.generate.stringify
        val email="alex2222"

        val emailDomain = "@gmail.com"
        println("insert fake existing user with id3:" + userId + " and email:" + (email + emailDomain))
        initDB(users, email, oldToken, new DateTime().minusDays(10).getMillis, userId)

        val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
          Json.obj(
            "login_provider" -> "facebook",
            "access_token" -> ("token_new" + testValidTokenEndToUse),
            "email" -> (email + emailDomain),
            "gender" -> "male",
            "first_name" -> "first_name",
            "last_name" -> "last_name",
            "gender" -> "male",

            "facebook_login_metadata" -> Json.obj(
              //"expirationTime" -> new DateTime().plusDays(1).getMillis,
              "expiresIn" -> "",
              //"username" -> "username1",
              "fbUserId" -> "string",
              "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
              "is_default_image"->false

            )
          )
        )).get
        status(response) must equalTo(OK)
        val jsonResponse: JsObject = contentAsJson(response).as[JsObject]
        println("resultJson " +  jsonResponse.toString())
        jsonResponse \ "status" === JsNumber(401)

      }


}
