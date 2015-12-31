package com.reviyou.services

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Format, Json}
import com.reviyou.common.RestStatusCodes._
import com.reviyou.models.Vote
import com.reviyou.models.dto.{SearchProfileDto, AggregateResult, VoteDto}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import com.reviyou.services.dao.{ProfileDao, VoteDao}
import com.reviyou.services.db.DBQueryBuilder

import play.api.libs.concurrent.Execution.Implicits._
import com.reviyou.services.exceptions.ServiceException

import scala.concurrent.Future


/**
 * Created by eugenezhgirov on 7/24/14.
 */
object VoteService {

  val log = LoggerFactory.getLogger(getClass)

  def storeVote(profileId: String, userId: String, voteValue: Int, isRevote: Boolean, skillId: Option[String] = None) :
  Future[JsObject] = {


    VoteDao.upsert(
      DBQueryBuilder.and(
        Json.obj("profile_id" -> profileId),
        Json.obj("user_id" -> userId),
        Json.obj("skill_id" -> skillId)),
      Vote(voteValue, DateTime.now().getMillis, profileId, skillId, userId, None)
    ) flatMap {
      case Right(vote) => {

        if (!isRevote) ProfileDao.incPopularCounts(profileId,1)

        val voteDto = vote.skill_id match {
          case None =>   updateGeneralRank(profileId)//for for profile
          case Some(s) => updateSkillRanks(profileId, vote.skill_id.get)//vote for skill
        }

        //voteDto.as[JsObject]
        voteDto
      }
      case Left(error) => Future(BaseServiceO.error(ERROR_UPDATE_OBJ, error.message))
    }
  }

  /**
   * update profile with a new votes count for the specific skill and general rank
   * after voting on profile's skill
   * @param profileId
   * @return our custom response in a json format(status0+data or statusError + message)
   */

  //TODO:simplify, move fields names to dao(Alex)
  def updateSkillRanks(profileId: String, skillId: String) : Future[JsObject] = {
    VoteDao.calcAvgSkillRank(profileId, skillId) flatMap {
      result =>
        val calcTupleRankVote = calcAverageRankAndVoteCount(result)
        import play.modules.reactivemongo.json.BSONFormats._


        ProfileDao.findAndModify(
          DBQueryBuilder.and(
            Json.obj("_id" -> BSONObjectID(profileId)),
            Json.obj("skills.skill_id" -> skillId)
          ),
          Json.obj("skills.$.skill_average_rank" -> calcTupleRankVote._1,
            "skills.$.votes_count" -> calcTupleRankVote._2
          )
        ).map(profileUpdateResult)
    }
  }

  /**
   * update profile with a new votes count and general rank after voting on profile
   * @param profileId
   * @return our custom response in a json format(status0+data or statusError + message)
   */
  def updateGeneralRank(profileId: String) : Future[JsObject] = {
    VoteDao.calcAvgProfileRank(profileId) flatMap {
      result =>
        val calcTupleRankVote = calcAverageRankAndVoteCount(result)

        ProfileDao.updateGeneralRank(profileId, calcTupleRankVote._1, calcTupleRankVote._2)
          .map(profileUpdateResult)
    }
  }

  /**
   *
   * @param result
   * @return our custom response in a json format(status0+data or statusError + message)
   */
  //TODO:simplify, too confusing move fields names to dao(Alex)
  def profileUpdateResult(result: Either[ServiceException, JsObject]) : JsObject = result match {
    case Right(updatedFields) =>

      val votesCount = updatedFields.fields
        .find(p => p._1 == "vtc" || p._1 == "skills.$.votes_count").map(_._2.as[Int])

      val returnedObj = VoteDto(
        updatedFields.\("gar").asOpt[Int],
        skill_average_rank = updatedFields.\("skills.$.skill_average_rank").asOpt[Int],
        votesCount
      )
      BaseServiceO.success(Json.toJson(returnedObj).as[JsObject])
    case Left(error) => BaseServiceO.error(ERROR_UPDATE_OBJ, error.message)
  }


  //returns avetage_vote and votes_count
  def calcAverageRankAndVoteCount(result: AggregateResult) : (Int, Int) = {
    if (result.iVotesCount > 0 && result.votesSum > 0) {
      //val averageRank = ((result.iVotesSum * result.votesCount * 100) / (result.iVotesCount * result.votesSum)).floor.toInt
      val voteValue = result.iVotesSum / result.iVotesCount

      //(averageRank, voteValue)
      ((voteValue * 10).floor.toInt, result.iVotesCount)
    } else (0, 0)
  }



  def getVotedProfiles(user_id:String, offset:Int) =  {

    val query =  DBQueryBuilder.query(DBQueryBuilder.and(Json.obj("user_id" -> user_id),
      Json.obj("show" -> Json.obj("$ne" -> false))))

    val futureProfileIdsResultsList: Future[List[Vote]] = VoteDao.find(
      query
     ++ DBQueryBuilder.orderBy(Json.obj("vote_time" -> -1))
      , startFrom =offset, upTo = 50, stopOnError = false)


    futureProfileIdsResultsList.map {list=>
      val profileIds = list.distinct map(vote => BSONObjectID(vote.profile_id))
      ProfileService.getProfilesByIds(profileIds)

    }
  }

  def hideVotedProfile(profile_id:String,user_id: String) = {
    log.trace(s"hideVotedProfile voted_profile_id: $profile_id, userId: $user_id")


    VoteDao.findAndModify(
      Json.obj("profile_id" -> profile_id, "user_id"->user_id),
      Json.obj("show" -> false)
    ) flatMap {
      case Right(b) => Future(BaseServiceO.success())
      case Left(e) => Future(BaseServiceO.error(ERROR_UPDATE_OBJ, e.message))
    }


  }


}
