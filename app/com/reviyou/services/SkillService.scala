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

/**
 * Created by zhgirov on 30.05.14.
 */
object SkillService {

  val MAX_SKILLS_RESULT = Play.application.configuration.getInt("query.skills.common.max.result").getOrElse(1000)

  val log = LoggerFactory.getLogger(getClass)


  /**
   * Gets all skills that have at least of the the specified tags assigned
   * { "tags": { $elemMatch: { $in: [  "tv2", "music" ] }} }
   * @param tags list of tags associated with the current profile
   * @return
   */
  def getSkills(tags:List[String]): Future[List[JsObject]] = {
    log.trace("getSkills for tags");
    val query = DBQueryBuilder.query(
      Json.obj(
        "tags" -> DBQueryBuilder.elemMatch(Json.obj("$in" -> tags))
      )
    )++ DBQueryBuilder.orderBy(Json.obj("skill_name" -> 1))
    SkillDao.find(query, 0, MAX_SKILLS_RESULT, stopOnError = true).map {
    list =>
      list.map(skill => convertSkillToDto(skill))
  }
}

  def convertSkillToDto(skill: Skill): JsObject = {
    log.trace("converSkillToDto " +  skill)
    Json.obj(
      "_id" -> skill._id.get.stringify,
      "skill_name" -> skill.skill_name
    )
  }

}
