package com.reviyou.services

import com.reviyou.common.dto.LoginRequest
import com.reviyou.models.LoginProvider
import dispatch._

//import scala.concurrent.ExecutionContext.Implicits.global
import java.io.InputStream
import java.net.URL

import org.slf4j.LoggerFactory
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
/**
 * Created by ALEXANDR on 4/19/14.
 */
object ImagesService {


  val log = LoggerFactory.getLogger(getClass)

  val ourDefaultImageUrl = Play.application.configuration.getString("image.default.url").get



  def isDefaultPicture(request: LoginRequest): Boolean = {
    val isDefault = request.login_provider match {
      case LoginProvider.Facebook => request.facebook_login_metadata.get.is_default_image
      case LoginProvider.Google => request.google_login_metadata.get.is_default_image
      case LoginProvider.Twitter => request.twitter_login_metadata.get.is_default_image
      case _ =>true
    }
    isDefault
    }


  /**
   * Methods loads user's image as if it's available in google\fb and saves it in the database
   * @param request
   * @return
   */
  def loadProfilePicture(request: LoginRequest): Future[InputStream] ={
    val url = isDefaultPicture(request) match {
      case true=>ourDefaultImageUrl
      case _ => {
        request.login_provider match {
          case LoginProvider.Facebook => s"${request.facebook_login_metadata.get.image_url}";
          case LoginProvider.Google => s"${request.google_login_metadata.get.image_url}";
          case LoginProvider.Twitter => s"${request.twitter_login_metadata.get.image_url}";
          case _ => ourDefaultImageUrl
        }
      }
    }

    val file1 =  new URL(url)
    log.trace(s"url of the new user image to download: $url")
    val fileIS:InputStream = file1.openStream()
    Future(fileIS)

    //https://graph.facebook.com/alexandr.lich/picture?width=100&height=100

  //google-      "image_url":"https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50",
    //    "is_default_image":true

  }


}
