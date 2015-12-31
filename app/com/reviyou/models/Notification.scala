package com.reviyou.models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json._

case class Notification(
                    override var _id: Option[BSONObjectID],
                    follower_id: String,
                    profile_id: String,
                    text:String,
                    //profile_name: String,
                    //new_reviews_count: Long,
                    create_time: DateTime,
                    acknowledge:Boolean
                    ) extends TemporalModel {

  override var updated: Option[DateTime] = None
  override var created: Option[DateTime] = None
}


object Notification {


  val createTimeFieldName = "ct"
  val followerFieldName = "fi"
  val ackFieldName = "a"
  import play.modules.reactivemongo.json.BSONFormats._

 // implicit val notificationFormat = Json.format[Notification]
  import play.modules.reactivemongo.json.BSONFormats._
  implicit val notification = new Format[Notification] {
    override def reads(json: JsValue): JsResult[Notification] = JsSuccess(Notification(
      (json \ "_id").asOpt[BSONObjectID],
      (json \ "fi").as[String],//follower user id
      (json \ "pi").as[String],//profile id for which notification is triggered
      (json \ "txt").as[String],//text
      //(json \ "pn").as[String],//profile name for which notification is triggered
      //(json \ "c").as[Long],//notification body: count of the comments added to the profile
      (json \ "ct").as[DateTime],//time when notification was created
      (json \ "a").as[Boolean] //notification acknowledged
    //todo: type of notification


    ))

    override def writes(notification: Notification): JsValue = {
      Json.obj("_id" -> notification._id,
        "fi" -> notification.follower_id,
        "pi" -> notification.profile_id,
        "txt" -> notification.text,
        //"pn" -> notification.profile_name,
        //"c" -> notification.new_reviews_count,
        "ct" -> notification.create_time,
        "a" -> notification.acknowledge
      )

    }
  }
}