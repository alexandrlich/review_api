package com.reviyou.services.dao

import com.reviyou.common.RestStatusCodes._
import com.reviyou.services.BaseServiceO

import reactivemongo.bson.BSONObjectID
import com.reviyou.services.db.DBQueryBuilder
import scala.concurrent.Future
import com.reviyou.models.{Job, Profile, ProfileState}
import com.reviyou.models.Profile._
import play.api.libs.json._

/* Implicits */
//import play.api.libs.concurrent.Execution.Implicits._


import play.modules.reactivemongo.json.ImplicitBSONHandlers._

/**
 * extends find methods for profile to retrieve only active(not deleted ones)
 *
 */
object ProfileDao extends DocumentDAO[Profile] {

  val collectionName = "profiles"


  // TODO validate query starts with either @query or @text
  val findActiveByIdTransformer = (__).json.update(
    __.read[JsObject].map {
      o => o ++ DBQueryBuilder.nin("state",List(ProfileState.Deleted, ProfileState.Pending, ProfileState.Rejected))
    })

  //any not deleted
  val findAnyByIdTransformer = (__).json.update(
    __.read[JsObject].map {
     //get any profile except deleted
      o => o ++DBQueryBuilder.nin("state",List(ProfileState.Deleted))
      //replace with ++ Json.obj(state->DBQueryBuilder.ne(ProfileState.Deleted))
    })





  /**
   *
   * make query to run against not deleted, not pending, not rejected profiles only
   *
   * if query can't be transformed - it replace it with a bad query intentionally to throw an error
   * rather than return incorrect results
   * @param query
   * @return
   */
  def findActive(query: JsObject, startFrom: Int = 0, upTo: Int = MAX_QUERY_RESULT, stopOnError: Boolean = true,
                 pjn: Option[JsObject] = None, sort: Option[JsObject] = None)(implicit reader: Reads[Profile]): Future[List[Profile]] = {
    log.trace(s"before transformation: $query")
    val newQuery = query.validate(findActiveByIdTransformer).getOrElse(Json.obj("invalid234"->true))
    super.find(newQuery, startFrom, upTo, stopOnError, pjn, sort)
  }

  /**
   * find any except deleted
   */
  def findAnyById(query: JsObject, startFrom: Int = 0, upTo: Int = MAX_QUERY_RESULT, stopOnError: Boolean = true,
  pjn: Option[JsObject] = None)(implicit reader: Reads[Profile]): Future[List[Profile]] = {
    log.trace(s"before transformation: $query")
    val newQuery = query.validate(findAnyByIdTransformer).getOrElse(Json.obj("invalid234"->true))
    super.find(newQuery, startFrom, upTo, stopOnError, pjn)
  }

  /**
   * not rejected not pending not deleted profile
   * @param id
   * @param reader
   * @return
   */
  def findActiveById(id: String)(implicit reader: Reads[Profile]): Future[Option[Profile]] = {
    log.trace(s"findActiveById: $id")
    val newQuery = DBQueryBuilder.id(id).validate(findActiveByIdTransformer).getOrElse(Json.obj("invalid234"->true))
    super.findOne(newQuery)
  }

  /**
   * not deleted profile
   * @param id
   * @param reader
   * @return
   */

  def findAnyById(id: String)(implicit reader: Reads[Profile]): Future[Option[Profile]] = {
    log.trace(s"findAnyById: $id")
    val newQuery = DBQueryBuilder.id(id).validate(findAnyByIdTransformer).getOrElse(Json.obj("invalid234"->true))
    super.findOne(newQuery)
  }


  def updateJobs(id: String,modifiedJobs:List[Job]):Future[JsObject] = {
    update(id, DBQueryBuilder.set("jobs", modifiedJobs)) map {
      case Right(b) => BaseServiceO.success()
      case Left(e) => BaseServiceO.error(ERROR_UPDATE_OBJ, e.message)
    }
  }

  /**
   * increase popular and comment counts
   * @param profile_id
   * @param popularIncreaseAmount increase by X points, could be negative if we remove comment
   * @param commentsTotalCount: current amount of comments
   * @return
   */
  def updatePopularAndCommentCounts(profile_id:String,popularIncreaseAmount:Int, commentsTotalCount:Int): Future[JsObject] = {
    //find and modify can't do inc very well with this driver so find. Then inc.
    for {
      f1<-incPopularCounts(profile_id, popularIncreaseAmount)
      f2<-updateCommentsCounts(profile_id, commentsTotalCount)
    } yield BaseServiceO.success()

  }

  def updateState(id: String, state:Int) = {
    update(id, DBQueryBuilder.set("state", state))
  }

  def incViewCount(id:String) = {
    update(id, DBQueryBuilder.inc("vwc", 1))
  }

  def incPopularCounts(id:String, amount:Int) = {
    update(id,
      DBQueryBuilder.inc("pi",1))
  }
  def updateCommentsCounts(id:String, count:Int) = {
    update(id,DBQueryBuilder.set("сtсt", count))
  }



  def findPopular( offset:Int, popular_fetch_size:Int, tags:List[String]) = {
    findActive(DBQueryBuilder.query(
      DBQueryBuilder.in("tags", tags)) ++
      DBQueryBuilder.orderBy(Json.obj("pi" -> -1)), startFrom = offset, upTo = popular_fetch_size)
  }

  def updateGeneralRank(id:String, general_rank:Int, votes_count:Int) = {
    import play.modules.reactivemongo.json.BSONFormats._
    findAndModify(
      Json.obj("_id" -> BSONObjectID(id)),
      Json.obj(
        "gar" -> general_rank,
        "vtc" -> votes_count
      )
    )
  }
}