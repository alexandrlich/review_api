package com.reviyou.controllers

import org.slf4j.LoggerFactory
import play.api.mvc._


object ShareController  extends BaseController {
  val log = LoggerFactory.getLogger(getClass)
  def share(profileId: String) = Action {
    log.trace(s"share, profile_id: $profileId")

    Ok(views.html.share())

  }


}