package com.reviyou.common.dto

import com.reviyou.models.{Settings, Device, UserModel, LoginProvider}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.reviyou.common.CustomException
import reactivemongo.bson.BSONObjectID

case class LoginRequest(
  first_name:String,
  last_name: String,
  email: String,
  login_provider: String,
  gender: Option[String],
  access_token: String,
  device: Option[DeviceLoginData] = None,
  facebook_login_metadata: Option[FbLoginData] = None,
  google_login_metadata: Option[GoogleLoginData] = None,
  twitter_login_metadata: Option[TwLoginData] = None
  ) {



  //needed here because of the def method below
  implicit val deviceDataWrites = (
    (__ \ "model").write[String] and
      (__ \ "uuid").write[String] and
      (__ \ "version").write[String] and
      (__ \ "platform").write[String] and
      (__ \ "notification_token").write[Option[String]]
    )(unlift(DeviceLoginData.unapply))

  implicit val deviceDataReads = (
    (__ \ "model").read[String] and
      (__ \ "uuid").read[String] and
      (__ \ "version").read[String] and
      (__ \ "platform").read[String] and
      (__ \ "notification_token").readNullable[String]

    )(DeviceLoginData.apply _)


  implicit val fbDataWrites = (
   // (__ \ "username").write[String] and
   //   (__ \ "expirationTime").write[Long] and
      (__ \ "expiresIn").write[String] and
        (__ \ "image_url").write[String] and
        (__ \ "is_default_image").write[Boolean] and
      (__ \ "fbUserId").write[String]
    )(unlift(FbLoginData.unapply))

  implicit val fbDataReads = (
    //(__ \ "username").read[String] and
    //  (__ \ "expirationTime").read[Long] and
      (__ \ "expiresIn").read[String] and
        (__ \ "image_url").read[String] and
        (__ \ "is_default_image").read[Boolean] and
      (__ \ "fbUserId").read[String]
    )(FbLoginData.apply _)

  implicit val googleDataWrites = (
      (__ \ "idToken").write[String] and
      (__ \ "expiresIn").write[Long] and
      (__ \ "refreshToken").write[String] and
      (__ \ "tokenType").write[String] and
        (__ \ "image_url").write[String] and
        (__ \ "is_default_image").write[Boolean] and
        (__ \ "googleUserId").write[String]
    )(unlift(GoogleLoginData.unapply))

  implicit val googleDataReads = (
      (__ \ "idToken").read[String] and
      (__ \ "expiresIn").read[Long] and
      (__ \ "refreshToken").read[String] and
      (__ \ "tokenType").read[String] and
        (__ \ "image_url").read[String] and
        (__ \ "is_default_image").read[Boolean] and
        (__ \ "googleUserId").read[String]
    )(GoogleLoginData.apply _)

  implicit val twDataWrites = (
    (__ \ "username").write[String] and
      (__ \ "image_url").write[String] and
      (__ \ "is_default_image").write[Boolean] and
      (__ \ "twUserId").write[String]
    )(unlift(TwLoginData.unapply))

  implicit val twDataReads = (
    (__ \ "username").read[String] and
      (__ \ "image_url").read[String] and
      (__ \ "is_default_image").read[Boolean] and
      (__ \ "twUserId").read[String]
    )(TwLoginData.apply _)


  //same code needs to be applied on UI: authorization.js to save in local storage
  def shortenedToken(token: String): String = token.take(20).mkString


  def createUserModel(userId: BSONObjectID = BSONObjectID.generate) : UserModel = {

    val devO:Option[Device] = device match {
      case Some(d) =>
        Some(Device(d.model, d.uuid, d.version, d.platform))
      case _ => None
    }

      UserModel(Some(userId),
              first_name,
              last_name,
              None,
              email.toLowerCase,
              login_provider,
              shortenedToken(access_token),
              None,
              devO,
              None, //bookmarks
              Settings(Some(true),Some(false)), //settings
              facebook_login_metadata,
              google_login_metadata,
              twitter_login_metadata)
  }

  def getUUID():String = {
    if(device.isEmpty) ""
    else device.get.uuid
  }

  //TODO:  maybe we should use UserModel constructor instead of implicit values
  @deprecated
  def existingUserModel(userId: String, tags:Option[List[String]], settings:Settings): JsObject = {


    import com.reviyou.models.UserModel._

    val base = Json.obj(
      "_id" -> Json.obj("$oid" -> userId),
      "first_name" -> first_name,
      "last_name" -> last_name,
      "login_provider" -> login_provider,
      "email" -> email,
      "device"-> device,
      "settings" ->Json.toJson(settings),
      "token" -> shortenedToken(access_token)//only save 20 digits in db
    )
    //hack
    val modifier1 = tags match {
      case Some(s) => base ++ Json.obj("tags" -> tags)
      case None => base
    }





    val modifier2 = {
      login_provider match {
        case LoginProvider.Facebook =>
          modifier1 ++ Json.obj("fb_login_data" -> facebook_login_metadata.get)
        case LoginProvider.Google =>
          modifier1 ++ Json.obj("gg_login_data" -> Json.toJson(google_login_metadata.get))
        case LoginProvider.Twitter =>
          modifier1 ++ Json.obj("tw_login_data" -> Json.toJson(twitter_login_metadata.get))
        case LoginProvider.Reviyou=>base
        case _ => throw CustomException.create("unsupported login provider")

      }
    }

    modifier2
  }

}


case class DeviceLoginData(model: String, uuid: String, version: String, platform: String, notificationToken:Option[String])

//removed 2 fields because fb api changed: https://developers.facebook.com/docs/reference/javascript/FB.getLoginStatus
//https://developers.facebook.com/docs/apps/changelog
case class FbLoginData(/*username: String, expirationTime: Long,*/ expiresIn: String, image_url: String, is_default_image : Boolean, fbUserId: String)

case class GoogleLoginData(idToken: String, expiresIn: Long, refreshToken: String,
        tokenType: String, image_url: String, is_default_image : Boolean, googleUserId: String)

case class TwLoginData(username: String,  image_url: String, is_default_image : Boolean, twUserId: String)



object LoginRequest  {

  //needed here duplicated with case class because of the parses

  implicit val deviceDataWrites = (
    (__ \ "model").write[String] and
      (__ \ "uuid").write[String] and
      (__ \ "version").write[String] and
      (__ \ "platform").write[String] and
      (__ \ "notification_token").write[Option[String]]
    )(unlift(DeviceLoginData.unapply))

  implicit val deviceDataReads = (
    (__ \ "model").read[String] and
      (__ \ "uuid").read[String] and
      (__ \ "version").read[String] and
      (__ \ "platform").read[String] and
      (__ \ "notification_token").readNullable[String]
    )(DeviceLoginData.apply _)

  implicit val fbDataWrites = (
  //    (__ \ "username").write[String] and
   //   (__ \ "expirationTime").write[Long] and
      (__ \ "expiresIn").write[String] and
        (__ \ "image_url").write[String] and
        (__ \ "is_default_image").write[Boolean] and
      (__ \ "fbUserId").write[String]
    )(unlift(FbLoginData.unapply))


  implicit val fbDataReads = (
    //  (__ \ "username").read[String] and
   //   (__ \ "expirationTime").read[Long] and
      (__ \ "expiresIn").read[String] and
        (__ \ "image_url").read[String] and
        (__ \ "is_default_image").read[Boolean] and
      (__ \ "fbUserId").read[String]
    )(FbLoginData.apply _)


  implicit val googleDataWrites = (
      (__ \ "idToken").write[String] and
      (__ \ "expiresIn").write[Long] and
      (__ \ "refreshToken").write[String] and
      (__ \ "tokenType").write[String] and
        (__ \ "image_url").write[String] and
        (__ \ "is_default_image").write[Boolean] and
        (__ \ "googleUserId").write[String]
    )(unlift(GoogleLoginData.unapply))


  implicit val googleDataReads = (
      (__ \ "idToken").read[String] and
      (__ \ "expiresIn").read[Long] and
      (__ \ "refreshToken").read[String] and
      (__ \ "tokenType").read[String] and
        (__ \ "image_url").read[String] and
        (__ \ "is_default_image").read[Boolean] and
        (__ \ "googleUserId").read[String]
    )(GoogleLoginData.apply _)

  implicit val twDataWrites = (
    (__ \ "username").write[String] and
      (__ \ "image_url").write[String] and
      (__ \ "is_default_image").write[Boolean] and
      (__ \ "twUserId").write[String]
    )(unlift(TwLoginData.unapply))

  implicit val twDataReads = (
    (__ \ "username").read[String] and
      (__ \ "image_url").read[String] and
      (__ \ "is_default_image").read[Boolean] and
      (__ \ "twUserId").read[String]
    )(TwLoginData.apply _)

  //should be the same order as fields in constructor
  implicit val loginRequestDataWrites = (
      (__ \ "first_name").write[String] and
      (__ \ "last_name").write[String] and
      (__ \ "email").write[String] and
      (__ \ "login_provider").write[String] and
      (__ \ "gender").write[Option[String]] and
      (__ \ "access_token").write[String] and
      (__ \ "device").write[Option[DeviceLoginData]] and
      (__ \ "facebook_login_metadata").write[Option[FbLoginData]] and
      (__ \ "google_login_metadata").write[Option[GoogleLoginData]] and
      (__ \ "twitter_login_metadata").write[Option[TwLoginData]]
    )(unlift(LoginRequest.unapply))

      implicit val loginRequestDataReads = (
          (__ \ "first_name").read[String] and
          (__ \ "last_name").read[String] and
          (__ \ "email").read[String] and
          (__ \ "login_provider").read[String] and
          (__ \ "gender").readNullable[String] and
          (__ \ "access_token").read[String] and
          (__ \ "device").readNullable[DeviceLoginData] and
          (__ \ "facebook_login_metadata").readNullable[FbLoginData] and
          (__ \ "google_login_metadata").readNullable[GoogleLoginData] and
          (__ \ "twitter_login_metadata").readNullable[TwLoginData]
        )(LoginRequest.apply _)
}
