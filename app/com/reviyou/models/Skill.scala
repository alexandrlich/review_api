package com.reviyou.models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
 * Skill object.
 * Predefined skills stored in a DB
 *
 * Created by zhgirov on 11.05.14.
 */
case class Skill(
                  override var _id: Option[BSONObjectID],
                  skill_name: String,
                  tags: List[String],
                  override var updated: Option[DateTime] = None,
                  override var created: Option[DateTime] = None
                  ) extends TemporalModel {
}

object Skill {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val skillFormat = Json.format[Skill]

}
