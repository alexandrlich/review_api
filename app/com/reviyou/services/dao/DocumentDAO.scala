package com.reviyou.services.dao


import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scala.concurrent.Future

import play.api.Play
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import com.reviyou.models.TemporalModel
import com.reviyou.services.exceptions.ServiceException
import com.reviyou.services.db.DBQueryBuilder
import reactivemongo.api.{Cursor, QueryOpts}

import play.api.Play.current


/**
 * Created by zhgirov on 23.04.14.
 *
 * idea described here: https://gist.github.com/almeidap/5685801
 */
trait DocumentDAO[T <: TemporalModel] extends BaseDao {

  val MAX_QUERY_RESULT = Play.application.configuration.getInt("query.max.result").getOrElse(100)
  lazy val collection = db.collection[JSONCollection](collectionName)

  val log = LoggerFactory.getLogger(getClass)

  def insert(document: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
    if (document._id.isEmpty) {
      document._id = Some(BSONObjectID.generate)
    }
    document.created = Some(DateTime.now)
    document.updated = Some(DateTime.now)
    log.debug(s"Inserting document: [collection=$collectionName, data=$document]")
    Recover(collection.insert(document)) {
      document
    }
  }

  def find(query: JsObject = Json.obj(), startFrom: Int = 0, upTo: Int = MAX_QUERY_RESULT, stopOnError: Boolean = true,
           pjn: Option[JsObject] = None, sort: Option[JsObject] = None)(implicit reader: Reads[T]):  Future[List[T]]  = {
    //will not work -> Logger.debug(s"Finding documents: [collection=$collectionName, query=$query]")
    log.trace(s"Finding documents: [collection=$collectionName, query=$query]")

    var result = collection.find(query).options(QueryOpts().skip(startFrom).batchSize(upTo))
    result = pjn.map(p => result.projection(p)).getOrElse(result)
    sort.map(s => result.sort(s)).getOrElse(result).cursor[T].collect[List](upTo, stopOnError)
  }


  def getCursor(query: JsObject = Json.obj() )(implicit reader: Reads[T]):  Cursor[T]  = {
    log.trace(s"getCursor: [collection=$collectionName, query=$query]")

    collection.find(query).cursor[T]
  }



  def findAndModify(query: JsObject, document: JsObject): Future[Either[ServiceException, JsObject]] = {
    log.debug(s"Finding and updating document: [collection=$collectionName, document=$document]")
    Recover(collection.update(query, DBQueryBuilder.set(document))) {
      document
    }
  }

  //  def find(query: JsObject = Json.obj())(implicit reader: Reads[T]): Future[List[T]] = {
  //    Logger.debug(s"Finding documents: [collection=$collectionName, query=$query]")
  //    collection.find(query).cursor[T].collect[List]()
  //  }

  def findById(id: String)(implicit reader: Reads[T]): Future[Option[T]] = findOne(DBQueryBuilder.id(id))

  def findById(id: BSONObjectID)(implicit reader: Reads[T]): Future[Option[T]] = findOne(DBQueryBuilder.id(id))

  def findOne(query: JsObject)(implicit reader: Reads[T]):  Future[Option[T]] = {
    //Logger.debug(s"Finding one: [collection=$collectionName, query=$query]")
    log.debug(s"findOne: [collection=$collectionName, query=$query]")

    collection.find(query).one[T]
  }

  def update(id: String, document: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
    document.updated = Some(DateTime.now)
    log.debug(s"Updating document: [collection=$collectionName, id=$id, document=$document]")
    Recover(collection.update(DBQueryBuilder.id(id), DBQueryBuilder.set(document))) {
      document
    }
  }

  /**
   * NOTE: Doens't update Some to None properly!
   * @param id
   * @param query
   * @return
   */
  def update(id: String, query: JsObject): Future[Either[ServiceException, JsObject]] = {
    val data = updated(query)
    log.debug(s"Updating by query: [collection=$collectionName, id=$id, query=$data]")
    Recover(collection.update(DBQueryBuilder.id(id), data)) {
      data
    }
  }


  //upsert- create new if no match found
  def upsert(id: String, document: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
    document.updated = Some(DateTime.now)
    log.debug(s"Upserting by query: [collection=$collectionName, id=$id, document=$document]")
    Recover(collection.update(DBQueryBuilder.id(id), Json.toJson(document), upsert = true)) {
      document
    }
  }

  //upsert- create new if no match found
  def upsert(id: String, query: JsObject): Future[Either[ServiceException, JsObject]] = {
    val data = updated(query)
    log.debug(s"Updating by query: [collection=$collectionName, id=$id, query=$data]")
    Recover(collection.update(DBQueryBuilder.id(id), data, upsert = true)) {
      data
    }
  }

  def upsert(query: JsObject, document: T)(implicit writer: Writes[T]): Future[Either[ServiceException, T]] = {
    document.updated = Some(DateTime.now)
    val data = updated(query)
    log.debug(s"Updating by query: [collection=$collectionName, query=$query, document=$document]")
    Recover(collection.update(query, Json.toJson(document), upsert = true)) {
      document
    }
  }

  def upsert(query: JsObject, content: JsObject)(implicit writer: Writes[T]): Future[Either[ServiceException, JsObject]] = {
    val data = updated(content)
    log.debug(s"Updating by query: [collection=$collectionName, query=$query, content=$data]")
    Recover(collection.update(query, data, upsert = true)) {
      data
    }
  }


  def push[S](id: String, field: String, data: S)(implicit writer: Writes[S]): Future[Either[ServiceException, S]] = {
    log.debug(s"Pushing to document: [collection=$collectionName, id=$id, field=$field data=$data]")
    Recover(collection.update(DBQueryBuilder.id(id), DBQueryBuilder.push(field, data)
    )) {
      data
    }
  }

  def addToSet[S](id: String, field: String, data: S)(implicit writer: Writes[S]): Future[Either[ServiceException, S]] = {
    log.debug(s"Adding to set to document: [collection=$collectionName, id=$id, field=$field data=$data]")
    Recover(collection.update(DBQueryBuilder.id(id), DBQueryBuilder.addToSet(field, data)
    )) {
      data
    }
  }



  def pull[S](id: String, field: String, query: S)(implicit writer: Writes[S]): Future[Either[ServiceException, Boolean]] = {
    log.debug(s"Pulling from document: [collection=$collectionName, id=$id, field=$field query=$query]")
    Recover(collection.update(DBQueryBuilder.id(id), DBQueryBuilder.pull(field, query))) {
      true
    }
  }

  def unset(id: String, field: String): Future[Either[ServiceException, Boolean]] = {
    log.debug(s"Unset from document: [collection=$collectionName, id=$id, field=$field]")
    Recover(collection.update(DBQueryBuilder.id(id), DBQueryBuilder.unset(field))) {
      true
    }
  }

  def remove(id: String): Future[Either[ServiceException, Boolean]] = remove(BSONObjectID(id))

  def remove(id: BSONObjectID): Future[Either[ServiceException, Boolean]] = {
    log.debug(s"Removing document: [collection=$collectionName, id=$id]")
    Recover(
      collection.remove(DBQueryBuilder.id(id))
    ) {
      true
    }
  }

  def remove(query: JsObject, firstMatchOnly: Boolean = false): Future[Either[ServiceException, Boolean]] = {
    log.debug(s"Removing document(s): [collection=$collectionName, firstMatchOnly=$firstMatchOnly, query=$query]")
    Recover(
      collection.remove(query, firstMatchOnly = firstMatchOnly)
    ) {
      true
    }
  }

  def uncheckedInsert[T](document: T)(implicit writer: Writes[T]): Unit = {
    log.debug(s"Unchecked insert: [collection=$collectionName, document=$document]")
    collection.uncheckedInsert(document)
  }

  def uncheckedInsertdef(id: String, query: JsObject): Unit = {
    val data = updated(query)
    log.debug(s"Updating by query: [collection=$collectionName, id=$id, query=$data]")
    collection.update(DBQueryBuilder.id(id), data)
  }

  def updated(data: JsObject) = {
    data.validate((__ \ '$set).json.update(
      __.read[JsObject].map {
        o => o ++ Json.obj("updated" -> DateTime.now)
      }
    )).fold(
        error => data,
        success => success
      )
  }

}