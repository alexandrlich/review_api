package com.reviyou.models

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

case class DevicesPushIntegration(
                    override var _id: Option[BSONObjectID],//uuid
                    user_id: Option[String],
                    model: String,
                    platform: String,
                    device_version: String,
                    token: String,
                    device_active: Boolean,
                    inactive_since:Option[DateTime]
                    ) extends TemporalModel {

  override var updated: Option[DateTime] = None // Some(DateTime.now())
  override var created: Option[DateTime] = None
}




object DevicesPushIntegration {



  import play.modules.reactivemongo.json.BSONFormats._
  implicit val devicesPushIntegrationFormat = Json.format[DevicesPushIntegration]

 /*
  import play.modules.reactivemongo.json.BSONFormats._
  implicit val notification = new Format[Notification] {
    override def reads(json: JsValue): JsResult[Notification] = JsSuccess(Notification(
      (json \ "_id").asOpt[BSONObjectID],
      (json \ "fi").as[String],//follower user id
      (json \ "pi").as[String],//profile id for which notification is triggered
      (json \ "pn").as[String],//profile name for which notification is triggered
      (json \ "c").as[Long],//notification body: count of the comments added to the profile
      (json \ "ct").as[DateTime],//time when notification was created
      (json \ "a").as[Boolean] //notification acknowledged
    //todo: type of notification


    ))

    override def writes(notification: Notification): JsValue = {
      Json.obj("_id" -> notification._id,
        "fi" -> notification.follower_id,
        "pi" -> notification.profile_id,
        "pn" -> notification.profile_name,
        "c" -> notification.new_reviews_count,
        "ct" -> notification.create_time,
        "a" -> notification.acknowledge
      )

    }
    */
  }