package com.reviyou.services.dao

import com.reviyou.models.{Profile, Skill}
import play.api.libs.json.{Json, Reads, JsObject}
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending

/**
 * Created by zhgirov on 11.05.14.
 */
object SkillDao extends DocumentDAO[Skill] {
  override val collectionName: String = "skills"

}
