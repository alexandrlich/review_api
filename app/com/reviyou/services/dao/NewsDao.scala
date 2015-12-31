package com.reviyou.services.dao

import com.reviyou.models.News

import play.api.libs.json.{Json, Reads, JsObject}
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.Count
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Descending


object NewsDao extends DocumentDAO[News] {
  override val collectionName: String = "news"


}
