package com.reviyou

import com.reviyou.controllers.filters.LoggingFilter
import org.slf4j.LoggerFactory
import play.api.{GlobalSettings, _}
import play.api.mvc._
import play.filters.gzip.GzipFilter
object Global extends GlobalSettings {

  val log = LoggerFactory.getLogger(getClass)

  override def onStart(app: Application) {
    log.info("Application has started")
  }

  override def onStop(app: Application) {
    log.info("Application shutdown...")
  }

  override def doFilter(next: EssentialAction): EssentialAction = {
    val action = super.doFilter(next)

    Filters(action, LoggingFilter, new GzipFilter())
  }

//  override def onError(request: RequestHeader, ex: Throwable) = {
//    Future.successful(InternalServerError(request, ex))
//  }
//
//  override def onHandlerNotFound(request: RequestHeader) = {
//    Future.successful(NotFound(request))
//  }

//  override def onBadRequest(request: RequestHeader, error: String) = {
//    log.error(s"onBadRequest: $error" )
//    Future.successful(Ok(Json.obj("status" ->400, "error_message" -> error)))
//  }
}