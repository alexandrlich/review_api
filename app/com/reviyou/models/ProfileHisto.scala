package com.reviyou.models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
 * Created by zhgirov on 10.05.14.
 */
case class ProfileHisto(override var _id: Option[BSONObjectID],
                        profile_id: String,
                        action: String,
                        value: String,
                        override var updated: Option[DateTime] = None,
                        override var created: Option[DateTime] = None) extends TemporalModel {

}

object ProfileHisto {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val profileHistoFormat = Json.format[ProfileHisto]
}
