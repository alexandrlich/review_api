package com.reviyou.models

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import reactivemongo.bson.BSONObjectID

/**
 * Created by eugenezhgirov on 7/25/14.
 */
//TODO: do we use it and for what?
case class Request(override var _id: Option[BSONObjectID],
                    objectType: String,
                    objectId: Option[JsObject],
                    override var updated: Option[DateTime] = None,
                    override var created: Option[DateTime] = None) extends TemporalModel {

}

object Request {
  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val requestFormat = Json.format[Request]
}