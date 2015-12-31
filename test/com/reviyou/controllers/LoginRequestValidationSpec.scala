package com.reviyou.controllers

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import com.reviyou.utils.MongoDBTestUtils._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.test.Helpers._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.test.FakeRequest
import play.api.libs.json.{JsNumber, JsObject, Json}
import org.joda.time.DateTime


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
//TODO: old test doesn't work, should test com.reviyou.services layer
@RunWith(classOf[JUnitRunner])
class LoginRequestValidationSpec extends Specification {

  val testValidTokenToUse="alex_fake_token_to_allow_junits_execution"

  "new user fb valid request format"  in withMongoDb  {implicit app =>

    //def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
    val prefix = "new_user_login_test" +  new java.util.Date().getTime
    val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
      Json.obj(
        "login_provider" -> "facebook",
        "access_token" -> "fake-token",
        "email" -> (prefix+"@gmail.com"),
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

        ),
        "google_login_metadata"-> Json.obj(
          "expiresIn" -> new DateTime().plusDays(1).getMillis,
          //"name" -> "name1",
          //"displayName" -> "displayName1",
          "idToken" -> "11",
          "tokenType" -> "tokenType1",
          "refreshToken" ->"refreshToken1",
          "googleUserId" -> "googleUserId1",
          "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
          "is_default_image"->false

        ),
        "device" -> Json.obj(
          "model" -> "test_model",
          "uuid" -> "test_uuid",
          "version" -> "test_version",
          "platform" -> "test_platform"
        )
      ))).get

    status(response) must equalTo(OK)
    val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
    println("jsonResponse: " + jsonResponse.toString())

    jsonResponse \ "status" === JsNumber(401)// since we can't pass a valid token - nothing else to test yet
  }


  "new user google valid request format"  in withMongoDb  {implicit app =>

    //def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
    val prefix = "new_user_login_test" +  new java.util.Date().getTime
    val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
      Json.obj(
        "login_provider" -> "google",
        "access_token" -> "fake-token",
        "email" -> (prefix+"@gmail.com"),
        "gender" -> "male",
        "first_name" -> "first_name",
        "last_name" -> "last_name",
        "gender" -> "male",

        "google_login_metadata"-> Json.obj(
          "expiresIn" -> new DateTime().plusDays(1).getMillis,
          //"name" -> "name1",
          //"displayName" -> "displayName1",
          "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
          "is_default_image"->false,
          "idToken" -> "11",
          "tokenType" -> "tokenType1",
          "refreshToken" ->"refreshToken1",
          "googleUserId" -> "googleUserId1"
        ),
        "device" -> Json.obj(
          "model" -> "test_model",
          "uuid" -> "test_uuid",
          "version" -> "test_version",
          "platform" -> "test_platform"
        )
      ))).get
    status(response) must equalTo(OK)
    val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
    println("jsonResponse: " + jsonResponse.toString())

    jsonResponse \ "status" === JsNumber(401)// since we didn't pass a valid request - exception
  }



  "new user fb invalid request format"  in withMongoDb  {implicit app =>

   //def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
    val prefix = "new_user_login_test" +  new java.util.Date().getTime
    val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
      Json.obj(
        "login_provider" -> "facebook",
        "access_token" -> "fake-token",
        "email" -> (prefix+"@gmail.com"),
        "gender" -> "male",
        "first_name" -> "first_name",
        "last_name" -> "last_name",
        "gender" -> "male",

        "facebook_login_metadata" -> Json.obj(
          //"expirationTime" -> new DateTime().plusDays(1).getMillis,
          "expiresIn" -> "test123",
          //"username" -> "username1",
          "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
           "is_default_image"->false,
     "fbUserId2" -> "string"   //incorrect key name on purpose
        ),
        "device" -> Json.obj(
          "model" -> "test_model",
          "uuid" -> "test_uuid",
          "version" -> "test_version",
          "platform" -> "test_platform"
        )

      ))).get
    status(response) must equalTo(OK)
    val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
    println("jsonResponse: " + jsonResponse.toString())

    jsonResponse \ "status" === JsNumber(10000)// since we didn't pass a valid request - exception
  }

  "new user google invalid request format"  in withMongoDb  {implicit app =>

    //def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
    val prefix = "new_user_login_test" +  new java.util.Date().getTime
    val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
      Json.obj(
        "login_provider" -> "google",
        "access_token" -> "fake-token",
        "email" -> (prefix+"@gmail.com"),
        "gender" -> "male",
        "first_name" -> "first_name",
        "last_name" -> "last_name",
        "gender" -> "male",
        "facebook_login_metadata"->"", //invalid on purpose
        "google_login_metadata"-> Json.obj(
          "expiresIn" -> new DateTime().plusDays(1).getMillis,
          //"name" -> "name1",
          //"displayName" -> "displayName1",
          "image_url" ->"http://ia.media-imdb.com/images/M/MV5BMTk1MjM3NTU5M15BMl5BanBnXkFtZTcwMTMyMjAyMg@@._V1_SY317_CR14,0,214,317_AL_.jpg",
          "is_default_image"->false,
          "idToken" -> "11",
          "tokenType" -> "tokenType1",
          "refreshToken" ->"refreshToken1",
          "googleUserId" -> "googleUserId1"
        ),
        "device" -> Json.obj(
          "model" -> "test_model",
          "uuid" -> "test_uuid",
          "version" -> "test_version",
          "platform" -> "test_platform"
        )

      ))).get
    status(response) must equalTo(OK)
    val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
    println("jsonResponse: " + jsonResponse.toString())

    jsonResponse \ "status" === JsNumber(10000)// since we didn't pass a valid request - exception
  }



}
