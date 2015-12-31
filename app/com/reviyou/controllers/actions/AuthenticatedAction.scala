package com.reviyou.controllers.actions

import com.reviyou.controllers.BaseController
import com.reviyou.services.AuthorizationService
import com.reviyou.services.dao.UserDao
import org.slf4j.LoggerFactory
import play.api._
import play.api.Play.current

import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._

import scala.concurrent._

//Implicits

import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by zhgirov on 24.04.14.
 *
 * We expect POST to pass userid+token in it's body
 * AND GET&REMOTE in it's URL
 */
object AuthenticatedAction extends ActionBuilder[Request] with BaseController {
  val log = LoggerFactory.getLogger(getClass)


  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[SimpleResult]) = {
    log.debug(s"body ${request.body}")

    val credentials = request.method match {
      case "POST" | "PUT" => request.body
      case "GET"| "DELETE" =>
        Json.obj("user_id" -> request.getQueryString("user_id"),
          "user_token" -> request.getQueryString("user_token"))
      case _ => errorAsyncResponse(401, "requests is odd and not currently supported")
    }

    credentials match {
      case JsObject(x) =>
        val userId = x.toMap.get("user_id").get.asOpt[String].getOrElse("")
        val token = x.toMap.get("user_token").get.asOpt[String].getOrElse("")

        AuthorizationService.tokenMatchValidation(userId, token).map(result => result).flatMap(
          if (_) {
            logging(request)
            block(request)
          } else {
            errorAsyncResponse(401, "Unknown user, try to relogin.")
          }
        )
      case _ => errorAsyncResponse(400, "User id is not passed properly.")
    }
  }

}

