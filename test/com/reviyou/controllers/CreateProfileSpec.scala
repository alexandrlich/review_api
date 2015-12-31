package com.reviyou.controllers

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
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.Date
import com.reviyou.utils.MongoDBTestUtils._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.reviyou.models.Profile
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.DurationInt
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import com.reviyou.services.dao.ProfileDao

/* Implicits */
//import play.api.libs.concurrent.Execution.Implicits._


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
//@RunWith(classOf[JUnitRunner])
class CreateProfileSpec extends Specification {

  val timeout: FiniteDuration = DurationInt(10).seconds

  def createProfile(prefix: String) = {

    println(s"--------------- create profiles with $prefix ")

    val profile: JsObject = Json.obj(
      "first_name" -> (prefix + "name2"),
      "last_name" -> (prefix + "last_name_1"),
      "email" -> (prefix + "2@gmail.com"),
      "jobs" -> Json.arr(
        Json.obj(
          "company" -> "Google",
          "occupation" -> "architect",
          "start_date" -> new Date(),
          "end_date" -> new Date()
        ),
        Json.obj(
          "company" -> "Facebook",
          "occupation" -> "architect",
          "start_date" -> new Date(),
          "end_date" -> new Date()
        )
      ),
      "skills" -> Json.arr(),
      "created" -> new Date().getTime())
    // Await.result(ProfileDao.save(Profile(profile)), Duration.Inf)
  }

  "Application" should {

    def initDB(colection: JSONCollection, prefix: String) = {

      var json = Json.obj(
        "first_name" -> (prefix + "name1"),
        "last_name" -> (prefix + "last_name_1"),
        "email" -> (prefix + "1@gmail.com"),
        "jobs" -> Json.arr(
          Json.obj(
            "company" -> "Google",
            "occupation" -> "architect",
            "start_date" -> new Date(),
            "end_date" -> new Date()
          ),
          Json.obj(
            "company" -> "Facebook",
            "occupation" -> "architect",
            "start_date" -> new Date(),
            "end_date" -> new Date()
          )
        ),
        "created" -> new Date())

      colection.insert(json).map(lastError => lastError)

      json = Json.obj(
        "first_name" -> (prefix + "name3"),
        "last_name" -> (prefix + "last_name_3"),
        "email" -> (prefix + "3@gmail.com"),
        "jobs" -> Json.arr(
          Json.obj(
            "company" -> "Google",
            "occupation" -> "architect",
            "start_date" -> new Date(),
            "end_date" -> new Date()
          ),
          Json.obj(
            "company" -> "Facebook",
            "occupation" -> "architect",
            "start_date" -> new Date(),
            "end_date" -> new Date()
          ),
          Json.obj(
            "company" -> "Microsoft",
            "occupation" -> "architect",
            "start_date" -> new Date(),
            "end_date" -> new Date()
          )
        ),
        "created" -> new Date().getTime())

      colection.insert(json).map(lastError => lastError)
    }

    "send id of existing email" in withMongoDb { implicit app =>
      //def profiles: JSONCollection = ReactiveMongoPlugin.db(app).collection[JSONCollection]("profiles")
      val prefix = "test1"
      val p = createProfile(prefix)

      println(s"-------- $p")
      val request = FakeRequest.apply(POST, "/api/1.0/rest/profile").withJsonBody(
        Json.obj(
          "first_name" -> ("new" + prefix + "first_name"),
          "last_name" -> ("new" + prefix + "last_name"),
          "email" -> (prefix + "2@gmail.com"),
          "jobs" -> Json.arr(
            Json.obj(
              "company" -> "Company1",
              "occupation" -> "occupation",
              "start_date" -> new Date(),
              "end_date" -> new Date()
            ),
            Json.obj(
              "company" -> "Company2",
              "occupation" -> "occupation",
              "start_date" -> new Date(),
              "end_date" -> new Date()
            ),
            Json.obj(
              "company" -> "Company3",
              "occupation" -> "occupation",
              "start_date" -> new Date(),
              "end_date" -> new Date()
            )
          ),
          "skills" -> Json.arr()
        )
      )

      val response = route(request)
      response.isDefined mustEqual true

      val result = Await.result(response.get, timeout)

      status(response.get) must equalTo(OK)

      contentAsString(response.get) must_!= "invalid json"
      //val result = route(FakeRequest(POST, "/profile").withJsonBody(
      //        Json.obj(
      //          "email"-> (prefix + "2@gmail.com")
      //        )
      //      )).get
      //      status(result) must equalTo(OK)
      //      contentAsJson(result).as[JsObject].value.get("id").get.as[String].length should_!= 0
    }

    "send id of created email" in withMongoDb { implicit app =>
      def profiles: JSONCollection = ReactiveMongoPlugin.db(app).collection[JSONCollection]("profiles")
      val prefix = "test2" + new Date().getTime
      val result = route(FakeRequest(POST, s"/api/1.0/rest/profile").withJsonBody(
        Json.obj(
          "first_name" -> ("new" + prefix + "first_name"),
          "last_name" -> ("new" + prefix + "last_name"),
          "email" -> ("new" + prefix + "2@gmail.com"),
          "jobs" -> Json.arr(
            Json.obj(
              "company" -> "Company1",
              "occupation" -> "occupation",
              "start_date" -> new Date(),
              "end_date" -> new Date()
            ),
            Json.obj(
              "company" -> "Company2",
              "occupation" -> "occupation",
              "start_date" -> new Date(),
              "end_date" -> new Date()
            ),
            Json.obj(
              "company" -> "Company3",
              "occupation" -> "occupation",
              "start_date" -> new Date(),
              "end_date" -> new Date()
            )
          ),
          "skills" -> Json.arr()
        )
      )).get
      status(result) must equalTo(OK)
      val resultJson: JsObject = contentAsJson(result).as[JsObject]
      resultJson should_!= null
      resultJson.value.size should_== 1
      //      val id = resultJson.value.get("id").get

      //      ProfileDao.findByEmail("new" + prefix + "2@gmail.com")

      //      val cursor: Cursor[JsObject] = profiles
      //        .find(Json.obj("email" -> ("new" + prefix + "2@gmail.com")))
      //        .sort(Json.obj("created"-> -1)).cursor[JsObject]
      //
      //      val futureProfilesJsonArray = cursor.collect[List]() map {
      //        profileList => profileList
      //      }
      //
      //      futureProfilesJsonArray.map { profile =>
      //        if(profile.length > 0) {
      //            id shouldEqual profile(0).value.get("_id").get.as[JsObject].value.get("$oid")
      //        }
      //      }
    }
  }
}
