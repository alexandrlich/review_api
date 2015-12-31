package com.reviyou.services.dao

import com.reviyou.models.UserModel
import com.reviyou.services.db.DBQueryBuilder
import play.api.libs.json.{Json, JsObject, Reads}
import reactivemongo.api.QueryOpts
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending

import scala.util.{Success, Failure}

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._

/**
 * Created by zhgirov on 25.04.14.
 */

object UserDao extends DocumentDAO[UserModel] {
  val collectionName = "users"


  val tag_name_field = "tags"


  //def findByIds(ids: List[JsObject])(implicit reader: Reads[UserModel]): Future[List[UserModel]] = findList(DBQueryBuilder.in("_id",ids))

  def findList(query: JsObject)(implicit reader: Reads[UserModel]): Future[List[UserModel]] = {
    log.debug(s"findList: [collection=$collectionName, query=$query]")

    val result = collection.find(query);
    result.cursor[UserModel].collect[List](MAX_QUERY_RESULT, true)

  }

  def findByEmailAndProvider(email: String, provider:String)(implicit reader: Reads[UserModel]): Future[Option[UserModel]] = {
    log.trace("findByEmailAndProvider " + email)
    val userModel = collection.find(Json.obj("email" -> email,"login_provider"->provider))
      .one[UserModel]

    return userModel
  }




}
