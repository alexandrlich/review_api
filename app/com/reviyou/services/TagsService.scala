package com.reviyou.services


import com.reviyou.models.{UserModel, Comment}
import com.reviyou.models.dto.{CommentDto}
import com.reviyou.services.exceptions.{CustomServiceException, UnexpectedServiceException, ServiceException}


import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Json}
import com.reviyou.services.dao.{TagDao, CommentDao}
import com.reviyou.services.db.DBQueryBuilder

import scala.concurrent.{Promise, Future, ExecutionContext}
import play.api._
import play.api.Play.current

//Implicit
import com.reviyou.models.Comment._
import com.reviyou.common.RestStatusCodes._
import ExecutionContext.Implicits.global


object TagsService extends BaseService {

  val log = LoggerFactory.getLogger(getClass)



  /**
   * find comments with 2 requests to db
   * @param offset - deprecated
   * @return
   */
  def findTags(offset: Int) = {
    log.trace("findTags")

    for {
      tagsList <- TagDao.find(DBQueryBuilder.query(Json.obj())
        ++DBQueryBuilder.orderBy(Json.obj("name" -> -1)), startFrom = 0, upTo = 10000 )

    } yield tagsList.map {tag=>Json.obj("name"->tag.name,"subtags"->tag.subtags)}
  }

  //db.users.find({name: /^pa/})

  /**
   * find all matches base on the initial input
   * @param firstChars
   * @return
   */
  def searchTags(firstChars: String) = {
    log.trace(s"searchTags, firstChars: $firstChars")

    for {
      tagsList <- TagDao.find(DBQueryBuilder.regex("name",("^"+firstChars))
        //++DBQueryBuilder.orderBy(Json.obj("name" -> -1))
      )

    } yield tagsList.map {tag=>Json.obj("name"->tag.name,"subtags"->tag.subtags)}
  }




}
