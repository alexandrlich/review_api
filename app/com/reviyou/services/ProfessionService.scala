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
import reactivemongo.bson.BSONObjectID
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject

import play.api.Play.current


object ProfessionService {

  val MAX_SKILLS_RESULT = 1000

  val log = LoggerFactory.getLogger(getClass)


  //TODO: add sorting by name
  def getCommonProfessions(): Future[List[JsObject]] = {
    log.trace("getCommonProfessions");
    ProfessionDao.find(Json.obj(), 0, MAX_SKILLS_RESULT, stopOnError = true).map {
    list =>
      list.map(profession => convertProfessionToDto(profession))
  }
}

  //TODO: why do we need this method? isn't dto == model this case?
  def convertProfessionToDto(profession: Profession): JsObject = {
    log.trace("converProfessionToDto " +  profession)
    Json.obj(
      "_id" -> profession._id.get.stringify,
      "profession_name" -> profession.profession_name
    )
  }

}
