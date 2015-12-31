package com.reviyou.services.dao

import com.reviyou.models.{UserCredencials, UserModel}
import com.reviyou.services.db.DBQueryBuilder
import play.api.libs.json.{Json, JsObject, Reads}
import reactivemongo.api.QueryOpts
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending

import scala.util.{Success, Failure}

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._


object UserCredencialsDao extends DocumentDAO[UserCredencials] {
  val collectionName = "user_credencials"

}
