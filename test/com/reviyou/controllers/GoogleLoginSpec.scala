package com.reviyou.controllers


import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.libs.concurrent.Execution.Implicits._
import com.reviyou.utils.MongoDBTestUtils._
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import scala._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.test.FakeRequest
import org.joda.time.DateTime

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
//TODO: old test doesn't work, should test com.reviyou.services layer
@RunWith(classOf[JUnitRunner])
class GoogleLoginSpec extends Specification {

  //val testValidTokenToUse="alex_fake_token_to_allow_junits_execution"

/*
      def initDB(collection: JSONCollection, prefix: String, expireDate: Long, userId: String) = {
       // println(s"initDB, prefix: ${prefix}, userId: ${userId}")
        val jsonGeneratedUser = Json.obj(
          "_id" -> Json.obj("$oid" -> userId),
          "first_name" -> "firstName",
          "last_name" -> "lastName",
          "email" -> (prefix+"@reviyou.com"),
          "login_data" -> Json.obj(
            "token" -> prefix,
            "expiration_time" -> expireDate
          ),
          "bookmarks" ->  Json.arr(),
          "user_profile_image" -> ""
        )

         collection.insert(jsonGeneratedUser).map(lastError=>lastError)

      }
*/


  //TODO: use correct enabeld googleAPI and googleUserId,
  "new user google login failed" in withMongoDb  {implicit app =>

    //def users: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")
    //initDB(users, prefix, new DateTime().plusDays(1).getMillis, userId)

    val prefix = "new_user_login_test" +  new java.util.Date().getTime
    val response = route(FakeRequest(POST, "/api/1.0/rest/login").withJsonBody(
      Json.obj(
        "login_provider" -> "google",
        "access_token" -> "fake-token",//would not pass token validation
        "email" -> (prefix+"@gmail.com"),
        "gender" -> "male",
        "first_name" -> "first_name",
        "last_name" -> "last_name",
        "gender" -> "male",

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

    /*
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


}
