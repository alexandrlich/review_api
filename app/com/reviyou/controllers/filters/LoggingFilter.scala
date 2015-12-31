package com.reviyou.controllers.filters

import com.reviyou.controllers.BaseController
import play.api.Logger
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.slf4j.LoggerFactory

/**
 * Created by ALEXANDR on 4/9/14.
 */
object LoggingFilter extends Filter with BaseController {
  private val log = LoggerFactory.getLogger(getClass)
  def apply(nextFilter: (RequestHeader) => Future[SimpleResult])
           (requestHeader: RequestHeader): Future[SimpleResult] = {
    val startTime = System.currentTimeMillis
    log.trace(s"${requestHeader.method} starts")
    nextFilter(requestHeader).map { result =>
      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime
      log.trace(s"${requestHeader.method} ${requestHeader.uri} " +
        s"ends and took ${requestTime}ms and returned ${result.header.status}")
      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }

  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[SimpleResult]) = {
    log.debug(s"body ${request.body}")

    logging(request)
    block(request)
  }
}
