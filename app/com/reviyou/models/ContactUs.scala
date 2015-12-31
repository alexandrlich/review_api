package com.reviyou.models

import org.joda.time.DateTime
import play.api.libs.json.{Json, JsObject}
import reactivemongo.bson.BSONObjectID

/**
 * Created by eugenezhgirov on 10/7/14.
 * Request to delete profile or add skill
 */
//TODO: rename to separate from Feedback=ContactUs functionality
case class ContactUs(override var _id: Option[BSONObjectID],
                      request_object: JsObject,
                      override var updated: Option[DateTime] = None,
                      override var created: Option[DateTime] = None) extends TemporalModel {

  //TODO: maybe add required date when request was created?

}

object ContactUs {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val contactUsFormat = Json.format[ContactUs]
}
