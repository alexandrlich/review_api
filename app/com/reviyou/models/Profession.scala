package com.reviyou.models

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

/**
 * Profession object.
 * Predefined professions stored in a DB
 *
 *
 */
case class Profession(
                  override var _id: Option[BSONObjectID],
                  profession_name: String,
                  override var updated: Option[DateTime] = None,
                  override var created: Option[DateTime] = None
                  ) extends TemporalModel {
}


object Profession {

  import play.modules.reactivemongo.json.BSONFormats._

  // For MongoDB serialization
  implicit val skillFormat = Json.format[Profession]

}