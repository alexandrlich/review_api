package com.reviyou.services.db

import play.api.libs.json.{JsValue, Writes, Json, JsObject}
import reactivemongo.bson.BSONObjectID

/* Implicits */

import play.modules.reactivemongo.json.BSONFormats._

/**
 * @author zhgirov on 17.04.14.
 */
object DBQueryBuilder {

  def id(objectId: String): JsObject = id(BSONObjectID(objectId))

  def id(objectId: BSONObjectID): JsObject = Json.obj("_id" -> objectId)

  def set(field: String, data: JsObject): JsObject = set(Json.obj(field -> data))

  def set[T](field: String, data: T)(implicit writer: Writes[T]): JsObject = set(Json.obj(field -> data))

  def set(data: JsObject): JsObject = Json.obj("$set" -> data)

  def set[T](data: T)(implicit writer: Writes[T]): JsObject = Json.obj("$set" -> data)


  def elemMatch[T](data: T)(implicit writer: Writes[T]): JsObject = Json.obj("$elemMatch" -> data)


  def push[T](field: String, data: T)(implicit writer: Writes[T]): JsObject = Json.obj("$push" -> Json.obj(field -> data))

  def pull[T](field: String, query: T)(implicit writer: Writes[T]): JsObject = Json.obj("$pull" -> Json.obj(field -> query))

  def unset(field: String): JsObject = Json.obj("$unset" -> Json.obj(field -> 1))

  def inc(field: String, amount: Int) = Json.obj("$inc" -> Json.obj(field -> amount))
  def inc(field1: String, field2:String, amount: Int) = Json.obj("$inc" -> Json.obj(field1 -> amount,field2->amount))

  def in[T](field: String, data: List[T])(implicit writer: Writes[T]) = Json.obj(field -> Json.obj("$in" -> data))

  def nin[T](field: String, data: List[T])(implicit writer: Writes[T]) = Json.obj(field -> Json.obj("$nin" -> data))

  def or(criterias: JsObject*): JsObject = Json.obj("$or" -> criterias)

  def ne(criterias: JsObject*): JsObject = Json.obj("$ne" -> criterias)

  def and(criterias: JsObject*): JsObject = Json.obj("$and" -> criterias)

  def addToSet[T](field: String, data: T)(implicit writer: Writes[T]): JsObject = Json.obj("$addToSet" -> Json.obj(field -> data))

  def gt[T](field: String, value: T)(implicit writer: Writes[T]) = Json.obj(field -> Json.obj("$gt" -> value))

  def lt[T](field: String, value: T)(implicit writer: Writes[T]) = Json.obj(field -> Json.obj("$lt" -> value))

  def query[T](query: T)(implicit writer: Writes[T]): JsObject = Json.obj("$query" -> query)

  def regex(field: String, query: String): JsObject = Json.obj(field ->Json.obj("$regex" -> query))

  def orderBy[T](query: T)(implicit writer: Writes[T]): JsObject = Json.obj("$orderby" -> query)
  
  def search[T](query: T)(implicit writer: Writes[T]): JsObject = Json.obj("$search" -> query)
  
  def text[T](query: T)(implicit writer: Writes[T]): JsObject = Json.obj("$text" -> query)

}