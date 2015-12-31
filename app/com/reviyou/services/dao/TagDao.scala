package com.reviyou.services.dao

import com.reviyou.models.{Tag, Profile, Skill}
import play.api.libs.json.{Json, Reads, JsObject}
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending


object TagDao extends DocumentDAO[Tag] {
  override val collectionName: String = "tags"

}
