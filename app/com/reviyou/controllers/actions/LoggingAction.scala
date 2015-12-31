package com.reviyou.controllers.actions

import com.reviyou.controllers.BaseController
import org.slf4j.LoggerFactory
import play.api.mvc.{SimpleResult, Request, ActionBuilder}

import scala.concurrent.Future

/**
 * Created by eugenezhgirov on 11/30/14.
 */
object LoggingAction extends ActionBuilder[Request] with BaseController {
  val log = LoggerFactory.getLogger(getClass)

  override protected def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[SimpleResult]): Future[SimpleResult] = {
    logging(request)
    block(request)
  }
}
