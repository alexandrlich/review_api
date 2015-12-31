package com.reviyou.services.dao

import com.reviyou.models.ApprovalLink
import play.api.libs.json.{JsObject, Reads}
import reactivemongo.api.indexes.IndexType.Ascending

import scala.concurrent.Future

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._


object ApprovalLinkDao extends DocumentDAO[ApprovalLink] {
  override val collectionName: String = "approvalLink"

}
