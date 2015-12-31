package com.reviyou.models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
 * Created by zhgirov on 23.05.14.
 */
case class Bookmark(override var _id: Option[BSONObjectID],
                    profile_id: String,
                    user_id: String,
                    override var updated: Option[DateTime] = None,
                    override var created: Option[DateTime] = None
                     ) extends TemporalModel {

}

object Bookmark {

  import play.modules.reactivemongo.json.BSONFormats._

  implicit val bookmarkFormat = Json.format[Bookmark]
}
