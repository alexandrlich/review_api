package com.reviyou.models

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

/**
 * Created by eugenezhgirov on 9/23/14.
 */
case class LoggingItem (override var _id: Option[BSONObjectID],
                        method: String,
                        uri: String,
                        remoteAddress: String,
                        requestTime: Long,
                        queryString: Option[Map[String, Seq[String]]] = None,
                        body: Option[String],
                        override var updated: Option[DateTime] = None,
                        override var created: Option[DateTime] = None) extends TemporalModel {
}

object LoggingItem {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val loggingFormat = Json.format[LoggingItem]
}