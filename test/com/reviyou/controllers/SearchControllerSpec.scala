package controllers

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.JsArray
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import com.reviyou.utils.MongoDBTestUtils._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class SearchDAOSpec extends Specification {

   val requiredParams = "&user_id=USERID&user_token=TOKEN"


    def initDB(colection: JSONCollection, prefix: String) = {

      var json = Json.obj(
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
        "first_name" -> (prefix + "name2"),
        "last_name" -> (prefix + "last_name_2"),
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

    "send empty list on fake query" in withMongoDb  {implicit app =>

      val response = route(FakeRequest(GET, "/api/1.0/rest/search?query=fake_query" + requiredParams)).get
      status(response) must equalTo(OK)


      val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
      //println("jsonResponse: " + jsonResponse.toString())

      jsonResponse \ "status" === JsNumber(0)
      jsonResponse \ "data" ===  Json.arr()

      //contentAsJson(jsonResponse).as[JsArray].value.length should_== 0
    }


    "send result list on last_name_1 search" in withMongoDb  {implicit app =>

      def profiles: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("profiles")
      val prefix = "test1"
      initDB(profiles, prefix)

      val response = route(FakeRequest(GET, "/api/1.0/rest/search?query=" + prefix + "last_name_1" + requiredParams)).get

      val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]

      jsonResponse \ "status" === JsNumber(0)
      val ids: Seq[JsValue] = jsonResponse \ "data" \\ "last_name"
      ids.length === 1
      val expected = JsString(prefix +"last_name_1")
      ids.head ===  expected
    }

    "send result list on last_name_3 search must contain the last job at Microsoft" in withMongoDb  {implicit app =>
      def profiles: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("profiles")
      val prefix = "test2withwork"
      initDB(profiles, prefix)

      val response = route(FakeRequest(GET, "/api/1.0/rest/search?query=" + prefix + "last_name_3" + requiredParams)).get
      val jsonResponse :JsObject  = contentAsJson(response).as[JsObject]
      jsonResponse \ "status" === JsNumber(0)
      val jsonResponseData:JsArray = (jsonResponse \ "data").as[JsArray]
      val lastCompany: JsValue = jsonResponseData.value.head.as[JsObject].value.get("last_company").get.as[JsValue]
      lastCompany shouldEqual JsString("Microsoft")
    }


}
