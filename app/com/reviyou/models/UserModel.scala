package com.reviyou.models

import play.api.libs.json.{Json, JsObject}
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import com.reviyou.common.dto.{TwLoginData, GoogleLoginData, FbLoginData}

/**
 * mongo generated uid(pk with index) is used by us as userID
 * @param first_name
 * @param last_name
 * @param email
 * @param fb_login_data
 * @param gg_login_data
 * @param tw_login_data
 */
case class UserModel(
                      override var _id: Option[BSONObjectID],
                      first_name: String,
                      last_name: String,
                      gender: Option[String],
                      email: String,
                      login_provider: String,
                      token: String,
                      tags: Option[List[String]],
                      device:Option[Device], //last mobile device used
                      bookmarks: Option[List[String]],
                      settings:Settings,
                      fb_login_data: Option[FbLoginData],
                      gg_login_data: Option[GoogleLoginData],
                      tw_login_data: Option[TwLoginData],
                      override var updated: Option[DateTime] = None,
                      override var created: Option[DateTime] = None
                      ) extends TemporalModel {


  def getEmail: String = {
    email
  }

  def getUserImageName: String = {
    getUserId+".png"
  }

  def getUserId: String = {
    _id.get.stringify
  }

  def getUserToken: String = {
    token
  }

  def isExpired: Boolean = {
    login_provider match {
     //expiresIn is 60 days(duration) and expiresTime is the actual date. See fb specification
        //latest plugin is no longer providing expirationTime so we should use expiresIn as well
      case "facebook" =>false
        //new DateTime(fb_login_data.get.expirationTime).isBeforeNow

      //since google doesn't have expirationTime we should use (time when token was created + expiresIn) compared to now
      //TODO: change to use expiresIn properly
      // not working currently
      //case "google" => new DateTime(gg_login_data.get.expiresIn).isBeforeNow
      case "google" => false
        //there is no expiration since it's not oauth 2.0, so always false
      case "twitter" => false
      case "reviyou" => false
    }

    //new DateTime(login_data.expiration_time).isBeforeNow
  }
}

case class Device(model: String, uuid: String, version: String, platform: String)

case class Settings(
                     appSounds: Option[Boolean],
                     followOnComment:Option[Boolean]
                     )

object LoginProvider  {
  val Reviyou:String = "reviyou"
  val Facebook:String = "facebook"
  val Google:String = "google"
  val Twitter:String = "twitter"

}



//TODO: add DEVICE case class
//TODO: separate by  fbLoginData and googleLoginData case classes maybe?
//case class LoginData(token: String, expiration_time: Long)

object UserModel {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val loginFbDataFormat = Json.format[FbLoginData]
  implicit val loginGgDataFormat = Json.format[GoogleLoginData]
  implicit val loginTwDataFormat = Json.format[TwLoginData]
  implicit val loginSettingsDataFormat = Json.format[Settings]



  implicit val loginDeviceFormat = Json.format[Device]
  implicit val userFormat = Json.format[UserModel]

}
