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
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json.toJsFieldJsValueWrapper


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class FindProfileByIdSpec extends Specification {

  "Application" should {

    def initDB(colection: JSONCollection, prefix: String) = {

      var json = Json.obj(
        "$oid" -> Json.obj("_id" -> "111111"),
        "first_name" -> (prefix + "name1"),
        "last_name" -> (prefix + "last_name_1"),
        "email" -> (prefix + "1@gmail.com"),
        "jobs" -> Json.arr(
          Json.obj(
            "company" -> "Google",
            "occupation" -> "architect",
            "start_date" -> new java.util.Date(),
            "end_date" -> new java.util.Date()
          ),
          Json.obj(
            "company" -> "Facebook",
            "occupation" -> "architect",
            "start_date" -> new java.util.Date(),
            "end_date" -> new java.util.Date()
          )
        ),
        "created" -> new java.util.Date())

      colection.insert(json).map(lastError=>lastError)

      json = Json.obj(
        "_id" -> Json.obj("$oid" -> "222222222222222222222222"),
        "first_name" -> (prefix + "name2"),
        "last_name" -> (prefix + "last_name_1"),
        "email" -> (prefix + "2@gmail.com"),
        "jobs" -> Json.arr(
          Json.obj(
            "company" -> "Google",
            "occupation" -> "architect",
            "start_date" -> new java.util.Date(),
            "end_date" -> new java.util.Date()
          ),
          Json.obj(
            "company" -> "Facebook",
            "occupation" -> "architect",
            "start_date" -> new java.util.Date(),
            "end_date" -> new java.util.Date()
          )
        ),
        "created" -> new java.util.Date().getTime())

      colection.insert(json).map(lastError=>lastError)

      json = Json.obj(
        "_id" -> Json.obj("$oid" -> "333333"),
        "first_name" -> (prefix + "name3"),
        "last_name" -> (prefix + "last_name_3"),
        "email" -> (prefix + "3@gmail.com"),
        "jobs" -> Json.arr(
          Json.obj(
            "company" -> "Google",
            "occupation" -> "architect",
            "start_date" -> new java.util.Date(),
            "end_date" -> new java.util.Date()
          ),
          Json.obj(
            "company" -> "Facebook",
            "occupation" -> "architect",
            "start_date" -> new java.util.Date(),
            "end_date" -> new java.util.Date()
          ),
          Json.obj(
            "company" -> "Microsoft",
            "occupation" -> "architect",
            "start_date" -> new java.util.Date(),
            "end_date" -> new java.util.Date()
          )
        ),
        "created" -> new java.util.Date().getTime())

      colection.insert(json).map(lastError=>lastError)
    }

    "find profile by id" in new WithApplication {
      def profiles: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("profiles")
      val prefix = "222222"
      initDB(profiles, prefix)
      val result = route(FakeRequest(GET, "/api/1.0/rest/profile/222222222222222222222222")).get
      status(result) must equalTo(OK)

      val obj: JsObject = contentAsJson(result).as[JsObject]
      println("res " + obj)
      obj should_!= null
      obj.value.get("id").get.as[String] shouldEqual "222222222222222222222222"
    }

  }
}
