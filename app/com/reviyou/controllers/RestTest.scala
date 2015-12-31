package com.reviyou.controllers

import com.reviyou.controllers.actions.AuthenticatedAction
import org.slf4j.LoggerFactory
import play.api._
import play.api.mvc._
import play.api.libs.json._
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.RawCommand

// you need this import to have combinators
import play.api.libs.functional.syntax._

object RestTest  extends Controller {

  val log = LoggerFactory.getLogger(getClass)


  //used by newrelic
  /*
  def newrelicCheck = Action { implicit request =>
    import reactivemongo.core.commands.Count

    val commandDoc =
      BSONDocument(
        "ping" -> "1"
      )
    try {

        // count the number of documents which tag equals "closed"
      val futureResult = db.command(RawCommand(commandDoc))

      futureResult.map { result => // result is a BSONDocument
         Nil
      }

    } catch {
      case e: NoSuchElementException => Ok(Json.obj("status" ->400, "error_message" -> "some params are missing"))
    }


  }*/

  def testServiceGet = Action { implicit request =>
    println("testServiceGet")
    try {
      val foo = request.queryString("foo")
      val bar = request.queryString("bar")
      if(!foo.isEmpty && !bar.isEmpty ) {
        Ok(Json.obj("status" ->0, "data" -> (s"Hello $foo your get request $bar")))
      } else {
        //Ok(Json.obj("status" ->9999, "error_message" ->"some params are empty"))
        BadRequest("some params are empty")
      }
    } catch {
      //case e: NoSuchElementException => BadRequest("some params are missing" + e.getMessage) //should be intercepted in filter somewhere? or do below
      case e: NoSuchElementException => Ok(Json.obj("status" ->400, "error_message" -> "some params are missing"))
    }

  }


  def testServiceGetSecured = AuthenticatedAction{ implicit request =>
    println("testServiceGetSecured")

  try {
      val foo = request.queryString("foo")
      val bar = request.queryString("bar")
      if(!foo.isEmpty && !bar.isEmpty ) {
        Ok(Json.obj("status" ->0, "data" -> (s"Hello $foo your get request $bar")))
      } else {
        //Ok(Json.obj("status" ->9999, "error_message" ->"some params are empty"))
        BadRequest("some params are empty")
      }
    } catch {
      //case e: NoSuchElementException => BadRequest("some params are missing" + e.getMessage) //should be intercepted in filter somewhere? or do below
      case e: NoSuchElementException => Ok(Json.obj("status" ->400, "error_message" -> "some params are missing"))
    }
  //Ok(Json.obj("version" ->"222"))
  }



  implicit val rds = (
    (__ \ 'foo).read[String] and
      (__ \ 'bar).read[Long]
    ) tupled

  def testServicePost = Action(parse.json) { request =>
    println("testServicePost")
    request.body.validate[(String, Long)].map{
      case (foo, bar) => Ok(Json.obj("status" ->0, "data" -> ("Hello " + foo+" , you're " + bar) ))
    }.recoverTotal{
      //e => BadRequest("Detected error:"+ JsError.toFlatJson(e))//will be intercepted in global ro filter?
        e => Ok(Json.obj("status" ->400, "error_message" -> JsError.toFlatJson(e) ))
    }
   }

  def version = Action { implicit request =>
    println("version")
    log.debug("versiondebug")
    log.info("versioninfo")
    import play.api.Play.current
    val vrsn = Play.application.configuration.getString("app.version").get
    Ok(Json.obj("version" ->vrsn))
  }
}
