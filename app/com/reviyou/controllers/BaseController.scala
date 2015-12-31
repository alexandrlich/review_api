package com.reviyou.controllers

import com.reviyou.models.LoggingItem
import com.reviyou.services.{BaseServiceO}
import com.reviyou.services.dao.LoggingItemDao
import org.joda.time.DateTime
import play.api.mvc.{Request, Controller, SimpleResult}
import scala.concurrent.Future
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsValue, JsArray, JsObject}

/**
 * Created by ALEXANDR on 4/19/14.
 */
trait BaseController extends Controller {

  val baselog = LoggerFactory.getLogger(getClass)


  def errorAsyncResponse(code: Int, errorMsg: String): Future[SimpleResult] = {
    baselog.error(s"code: $code, message: $errorMsg.")
    Future.successful(Ok(BaseServiceO.error(code,errorMsg)))
  }

  def errorAsyncResponse(code: Int, errorMsg: String, detailedMsg: String): Future[SimpleResult] = {
    baselog.error(s"code: $code, message: $errorMsg.")
    Future.successful(Ok(BaseServiceO.error(code,errorMsg,detailedMsg)))
  }

  def successAsyncResponse(data: JsObject): Future[SimpleResult] = {
    Future.successful(Ok(BaseServiceO.success(data)))
  }

  def errorResponse(code: Int, errorMsg: String): SimpleResult = {
    baselog.error(s"code: $code, message: $errorMsg.")
    Ok(BaseServiceO.error(code,errorMsg))
  }

  def errorResponse(code: Int, errorMsg: String, detailedMsg: String): SimpleResult = {
    baselog.error(s"code: $code, message: $errorMsg.")
    Ok(BaseServiceO.error(code,errorMsg,detailedMsg))
  }

  def successResponse(data: JsObject): SimpleResult = {
    Ok(BaseServiceO.success(data))
  }

  def getStatus(j : JsObject) :Int = {
    (j \ "status").as[Int]
  }
  def getErrorMessage(j : JsObject) :String = {
    (j \ "error_message").as[String]
  }

  /**
   * Generic response which could contain custom error or success data inside, just a pass by json to ui,
   * when returning object value is handled in service layer
   * @param js
   * @return
   */
  def response(js:JsObject): SimpleResult = {
    Ok(js)
  }


  def successResponse(data: JsArray): SimpleResult = {
    Ok(BaseServiceO.success(data))
  }

  def logging[A](request: Request[A]) = {
    LoggingItemDao.uncheckedInsert(
      LoggingItem(None,
        request.method,
        request.uri,
        request.remoteAddress,
        DateTime.now().getMillis,
        Some(request.queryString),
        Some(request.body.toString)
      )
    )
  }
}
