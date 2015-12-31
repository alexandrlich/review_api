package com.reviyou.services

import play.api.Play
import com.reviyou.services.dao._
import play.api.libs.json.{JsValue, Json}
import com.reviyou.common.Utils._
import com.reviyou.common.RestStatusCodes._
import play.api.libs.concurrent.Execution.Implicits._
import com.reviyou.models._

import scala.collection.Map
import scala.concurrent.Future
import com.reviyou.services.db.DBQueryBuilder
import reactivemongo.bson.{BSONDateTime, BSONTimestamp, BSONObjectID}
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject
import org.joda.time.DateTime
import com.reviyou.models.MongoJsFormat._
import play.api.Play.current

/**
 * Created by zhgirov on 30.05.14.
 */
object NewsService {

  val log = LoggerFactory.getLogger(getClass)


  //show the last 100
  def getNews(): Future[List[JsObject]] = {
    log.trace("getNews");
    NewsDao.find(DBQueryBuilder.query(Json.obj())
      ++DBQueryBuilder.orderBy(Json.obj("create_time" -> -1)), startFrom = 0, upTo = 100 )
    .map {
    list =>
      list.map(skill => convertNewsToDto(skill))
  }
}

  def checkRecentNews(): Future[JsObject] = {

    val recentDate  = DateTime.now().minusDays(7)
    log.trace("recentDate:"+ recentDate)

    NewsDao.findOne(DBQueryBuilder.query(DBQueryBuilder.gt("create_time", recentDate))).map {elementO=>
      elementO match {
        case Some(_) => {
          Json.obj("recentNewsExist" -> true)
        }
        case None => Json.obj("recentNewsExist" -> false)
      }

    }

  }

  def convertNewsToDto(news: News): JsObject = {
    val recentDate  = DateTime.now().minusDays(7)
    log.trace("convertNewsToDto, recentDate " +  recentDate)
    val isArticleRecent:Boolean = recentDate.isBefore(news.create_time.toDate.getTime)
    Json.obj(
      "_id" -> news._id.get.stringify,
      "title" -> news.title,
      "content" -> news.content,
      "date" -> news.create_time.toDate.getTime,
      "recent"->isArticleRecent
    )
  }

}
