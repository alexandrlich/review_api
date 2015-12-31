package com.reviyou.services.dao

import com.reviyou.models.dto.AggregateResult
import com.reviyou.models.dto.AggregateReturnedResult
import AggregateResult._
import AggregateReturnedResult._
import com.reviyou.models.Vote
import play.api.Logger
import play.api.libs.json.{Reads, Json, JsObject}
import reactivemongo.bson._
import reactivemongo.core.commands._
import reactivemongo.api.indexes.IndexType.Ascending

import scala.concurrent.Future

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
/**
 * Created by zhgirov on 11.05.14.
 */
object VoteDao extends DocumentDAO[Vote] {
  import play.modules.reactivemongo.json.BSONFormats
  import play.modules.reactivemongo.json.BSONFormats._

  override val collectionName: String = "votes"

  def calcAvgSkillRank(profileId: String, skillId: String) = {
    Logger.debug(s"Calculate average skill rank: [profileId = $profileId, skillId = $skillId]")

    val matchSkill = Match(BSONDocument("skill_id" -> skillId, "profile_id" -> profileId)) //TODO: profile_id should be replaced when formula clarified
    val groupProfileSkill = Group(BSONDocument("profile_id" -> "$profile_id", "skill_id" -> "$skill_id"))(("votesCount", SumValue(1)), ("votesSum", SumField("vote_value")))
    //{$group: _id: "$_id.skill_id", sumCount: {$sum: "$count"}, sumSum: {$sum: "$sum"}}
    //val groupByProfile = Group(BSONDocument("profile_id" -> profileId))(("totalCount", SumField("skillCount")), ("totalSum", SumField("skillSum")))
    val project = Project(("_id", BSONString("$_id.profile_id")),
      ("votesCount", BSONString("$votesCount")), ("votesSum", BSONString("$votesSum")))
    val aggDoc = Aggregate(collectionName, Seq(matchSkill, groupProfileSkill, project))

    // we get a Future[BSONDocument]
    val futureResult = db.command(aggDoc)

    futureResult map (result =>
      aggregateResult(profileId, result))
  }

  def calcAvgSkillsRank(profileId: String, skillIds: List[String]) = {
    Logger.debug(s"Calculate average skills rank: [profileId = $profileId, skillIds = $skillIds]")

    skillIds map {
      skillId =>
      val matchSkill = Match(BSONDocument("skill_id" -> skillId, "profile_id" -> profileId)) //TODO: profile_id should be replaced when formula clarified
      val groupProfileSkill = Group(BSONDocument("profile_id" -> "$profile_id", "skill_id" -> "$skill_id"))(("votesCount", SumValue(1)), ("votesSum", SumField("vote_value")))
      //{$group: _id: "$_id.skill_id", sumCount: {$sum: "$count"}, sumSum: {$sum: "$sum"}}
      //val groupByProfile = Group(BSONDocument("profile_id" -> profileId))(("totalCount", SumField("skillCount")), ("totalSum", SumField("skillSum")))
      val project = Project(("_id", BSONString("$_id.profile_id")),
        ("votesCount", BSONString("$votesCount")), ("votesSum", BSONString("$votesSum")))
      val aggDoc = Aggregate(collectionName, Seq(matchSkill, groupProfileSkill, project))

      // we get a Future[BSONDocument]
      val futureResult = db.command(aggDoc)

      futureResult map (result =>
        aggregateResult(profileId, result))
    }
  }

  def calcAvgProfileRank(profileId: String) = {
    log.debug(s"Calculate average rank: [profileId=$profileId]")

    val group = Group(BSONDocument("profile_id" -> "$profile_id"))(("votesCount", SumValue(1)), ("votesSum", SumField("vote_value")))

    val _match = Match(BSONDocument("skill_id" -> BSONDocument("$exists" -> false), "profile_id" -> profileId))//TODO: profile_id should be replaced when formula clarified
    val project = Project(("_id", BSONString("$_id.profile_id")),
      ("votesCount", BSONString("$votesCount")), ("votesSum", BSONString("$votesSum")))
    val aggDoc = Aggregate(collectionName, Seq(_match, group, project))

    // we get a Future[BSONDocument]
    val futureResult = db.command(aggDoc)

    futureResult map (result =>
      aggregateResult(profileId, result))
  }

  def aggregateResult(profileId: String, result: Stream[BSONDocument]) = {
    if (result.isEmpty) {
      AggregateResult()
    } else {
      result.par.foldLeft(AggregateResult()) { (result, d) =>

        val doc = Json.toJson(d).as[AggregateReturnedResult]

        if (profileId == doc._id) {
          AggregateResult(doc._id,
            doc.votesCount, doc.votesSum,
            result.votesCount + doc.votesCount, result.votesSum + doc.votesSum)
        } else {
          AggregateResult(result._id,
            result.votesCount, result.votesSum,
            result.votesCount + doc.votesCount, result.votesSum + doc.votesSum)
        }
      }
    }
  }

}
