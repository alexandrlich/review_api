package com.reviyou.services.dao

import com.reviyou.models.Profession
import play.api.libs.json.{Json, Reads, JsObject}
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending


object ProfessionDao extends DocumentDAO[Profession] {
  override val collectionName: String = "professions"

}
