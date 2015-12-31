package com.reviyou.models

import play.api.libs.json.{Json, JsObject}
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime

/**
 * Created by zhgirov on 02.05.14.
 *
 * contact us option
 */
case class Feedback(user_id: String, subject: String, description: String,
                    override var updated: Option[DateTime] = None,
                    override var created: Option[DateTime] = None) extends TemporalModel {

  override var _id: Option[BSONObjectID] = None

}

object Feedback {
  implicit val formatFeedback = Json.format[Feedback]
}
