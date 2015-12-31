package com.reviyou.services

import com.reviyou.models.ProfileState
import com.reviyou.services.db.DBQueryBuilder.{in, text, search, and}
import com.reviyou.services.exceptions.ServiceException
import play.api.Play
import com.reviyou.services.dao._
import play.api.cache.Cache
import play.api.libs.json.{JsValue, Json}
import com.reviyou.common.Utils._
import com.reviyou.common.RestStatusCodes._
import play.api.libs.concurrent.Execution.Implicits._
import com.reviyou.models.dto._
import com.reviyou.models._
import com.reviyou.models.Profile._
import scala.collection.Map
import scala.concurrent.Future
import com.reviyou.services.db.DBQueryBuilder
import reactivemongo.bson.BSONObjectID
import org.slf4j.LoggerFactory
import com.reviyou.models.Job
import com.reviyou.models.ProfileSkill
import play.api.libs.json.JsObject
import com.reviyou.common.Utils
import play.modules.reactivemongo.json.BSONFormats._

import play.api.Play.current


object ProfileService extends BaseService {

  val log = LoggerFactory.getLogger(getClass)

  //limits the amount of matching results
  val MAX_QUERY_RESULT = Play.application.configuration.getInt("query.max.result").getOrElse(100)
  val POPULARS_FETCH_SIZE = Play.application.configuration.getInt("populars.fetch.size").getOrElse(10)


  /**
   * capitalize each word in a string
   * @param str
   * @return
   */
  def toCamelCase(str: String): String = {
    str.toLowerCase.split(' ').map(_.capitalize).mkString(" ")
  }


  /**
   * Full text search of profiles in db. Search fields include email, first name, last name, company, occupation.
   *
   * if tags are not specified - search in all categories
   *
   * @param query user specified query.
   * @return set of fields for the search result page(not the whole document, only required fields to show)
   */
  def searchProfiles(query: String, tags: List[String], offset: Int): Future[List[JsObject]] = {
    log.trace(s"searchProfiles, query: $query")
    // db.profiles.find({$and:[{$text:{$search:"clint"}}, {"tags":{$in:["tv", "soccer"]}}, {"os":{$exists:true}}]}, { score: { $meta: "textScore" }, "os.$":1 }).sort({ score: { $meta: "textScore" } })

    val filterQuery:JsObject = tags.isEmpty match {
      case true =>text(search(query))
      case false =>and(text(search(query)), in("tags", tags))
    }



    val projSort = Some(Json.obj("score" -> Json.obj("$meta" -> "textScore")))

    // entries will be sorted by relevance
    val futureProfilesResultsList: Future[List[Profile]] = ProfileDao.findActive(
      filterQuery, upTo = MAX_QUERY_RESULT, stopOnError = false, pjn = projSort, sort = projSort, startFrom = offset
    )

    futureProfilesResultsList.map { list => list.map{profile =>

      Json.toJson[SearchProfileDto](SearchProfileDto(
        Some(profile._id.get.stringify),
        profile.name,
        profile.getInitials,
        profile.getTheme,
        maskProfileEmail(profile),
        profile.getSmallImageUrl,
        profile.comments_count.getOrElse(0),
        profile.getVotesCount,
        profile.getLastOccupation,
        profile.getLastCompanyName,
        profile.getAverageRank //profile rank
      )).as[JsObject]
     }
    }
  }


  /**
   * Checks if profile with the same email exist
   * (without approval we were also checking for first+last+occupation)
   * and not in rejected\deleted\pending status.
   *
   * If not - creates
   * @param jsProfileRequest
   * @return
   */
  def createPending(jsProfileRequest: ProfileRequest): Future[JsObject] = {
    log.trace("create: ")


    val searchQuery =Json.obj("email" -> jsProfileRequest.email.trim.toLowerCase)

    val newProfileId: BSONObjectID = BSONObjectID.generate



    ProfileDao.findOne(searchQuery).flatMap {
      case Some(profile) => {
        profile.getState match {
          case ProfileState.Pending =>
            Future(BaseServiceO.error(ERROR_PROFILE_EXISTS, "Profile exist, but not approved yet. You can contact owner directly and ask for an approval."))
          case ProfileState.Approved | ProfileState.Waived =>
            Future(BaseServiceO.error(ERROR_PROFILE_EXISTS, "Profile with such email already exist in our, search it or try creating another profile with unique name please."))
          case ProfileState.Rejected | ProfileState.Deleted  =>
            createPendingProfile(jsProfileRequest, newProfileId)
        }
      }
      case _ => createPendingProfile(jsProfileRequest, newProfileId)
    }
  }

  /**
   * create pending profile and send an email with a custom link for profile approval
   * @param jsProfileRequest
   * @param newProfileId
   * @return
   */
  def createPendingProfile(jsProfileRequest:ProfileRequest, newProfileId: BSONObjectID): Future[JsObject] = {
    val uid: BSONObjectID = BSONObjectID.generate

    ApprovalLinkDao.insert(ApprovalLink(Some(uid), newProfileId.stringify)) flatMap {
      case Left(left) => Future(BaseServiceO.error(ERROR_INSERT_OBJ_TO_DB, left.message))
      case Right(right)=> {
        UserDaoOld.findByUserId(jsProfileRequest.user_id) flatMap {
          case Some(owner) => {
            insertPending(jsProfileRequest, newProfileId) map {
              case Right(b) => EmailService.sendProfileApprovalRequest(owner,jsProfileRequest,uid.stringify)
              case Left(e) => BaseServiceO.error(ERROR_INSERT_OBJ_TO_DB, e.message)
            }
          }
          case _ => Future(BaseServiceO.error(ERROR_USR_NOT_FOUND, "user not found: " + jsProfileRequest.user_id ))

        }

       }
      }
  }


  /**
   * Creates profile with pending status and not active,email is lower cased everything else - camel
   * @param profileRequest
   * @return
   */
  def insertPending(profileRequest: ProfileRequest, newProfileId: BSONObjectID): Future[Either[ServiceException, Profile]] = {
    log.trace("insert: ")

    val jobs: List[Job] = profileRequest.jobs map {
      job =>

        Job(
          job.companies,
          Some(toCamelCase(job.occupation.get)),
          job.start_date,
          job.end_date,
          Some(job.end_date.isEmpty)
        )
    }
    //TODO: move to the formatter of profileRequest
    val email = profileRequest.email.trim match {
      case "" => None
      case x => Option(x.trim.toLowerCase)
    }
    val profile = Profile(
      Some(newProfileId),//_id
      toCamelCase(profileRequest.name),
      email, //email in lower case
      None,//image url
      None,//big image url
      None,//db
      Some(profileRequest.user_id),
      Some(profileRequest.theme_name),
      None, //initial view count
      None, //comments count
      None, //general votes rank
      None, //general votes count
      jobs, //camelCased jobs
      None,//tags
      None,//skills
      None, //popular_index
      None //state

    )


    ProfileDao.insert(profile)
    /*
    flatMap {
      case Left(left) => Future(BaseServiceO.error(ERROR_INSERT_OBJ_TO_DB, left.message))
      case Right(right) => Future(BaseServiceO.success(transformProfileToDto(right))) //transformProfileToDto(right) map ((OK, _)) //
    }*/
  }

  /**
   * Consolidate the following information about the profile
   *
   * 1.profile itself if it's not marked as 'deleted'(but could be rejected or pending)
   * 2.whether it's bookmarked by current user
   * 3.previous votes made by this user
   * 4.comments count
   * 5.whether or not anyone else left comments(except me) - used to decide if profile can be deleted
   *
   * @param profileId
   * @param userId
   * @return
   */
  def getProfileViewById(profileId: String, userId: String): Future[JsObject] = {
    log.trace(s"findById: $profileId, userId: $userId")
    for {
      maybeProfile <- ProfileDao.findActiveById(profileId)
      hasOtherPeopleComments <- CommentDao.otherPeopleComments(profileId, userId)
      profileBookmarkByUser <- BookmarkDao.find(DBQueryBuilder.query(Json.obj("profile_id" -> profileId, "user_id" -> userId)))
      previousProfileVotesByUser <- VoteDao.find(Json.obj("profile_id" -> profileId, "user_id" -> userId))
    } yield maybeProfile.map { profile =>
      incViewCounts(profile._id.map(_.stringify).get, userId) //inc unique visitors

      log.debug(s"findById profile ${Json.toJson[Profile](profile).as[JsObject]}")

      val isBookmarkedByCurrent = profileBookmarkByUser.headOption match {
        case Some(b) => true
        case None => false
      }

      BaseServiceO.success(transformProfileToDto(profile, userId, isBookmarkedByCurrent, hasOtherPeopleComments, previousProfileVotesByUser))
    }.getOrElse(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, s"No active profile found for id = $profileId"))

    //todo: not sure if we need here yieldRes onFailure? for ex is userId is "undefined" it'll blow up

  }

  /*
    Upon profile view
    1.add unique visitor

    2.whether or not it's unique visitor - increase views_count by 1
   */
  def incViewCounts(profileId: String, userId: String) = {
    log.trace(s"incViewCounts: $profileId, userId: $userId")


    ProfileVisitorsDao.findOne(DBQueryBuilder.query(
      Json.obj("_id" -> profileId,
        "user_ids" -> DBQueryBuilder.elemMatch(Json.obj("$eq" -> userId))
      )
    )).map { res =>
      ProfileDao.incViewCount(profileId)

      res match {
        case Some(pv) => {}
        case None => {
          ProfileVisitorsDao.upsert(profileId, DBQueryBuilder.addToSet("user_ids", userId))
        }
      }
    }
  }

  //adding common skill to profile
  def addSkill(skillId: String, profileId: String, userId: String): Future[JsObject] = {
    log.trace(s"addSkill, skillId: $skillId")
    SkillDao.findById(skillId) flatMap {
      case Some(s) => addSkillToProfile(s, userId, profileId)
      case _ => Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, s"Skill[id = $skillId] has not been found."))
    }
  }


  //adding common skill to profile
  def addSkillToProfile(skill: Skill, userId: String, profile_id: String): Future[JsObject] = {
    log.trace(s"addSkillToProfile, skill: $skill")
    ProfileDao.findActiveById(profile_id) flatMap {
      //anyone should be able to add existing common skill to any profile
      //case Some(profile) if profile.user_id != userId =>
      // Future(ERROR_UPDATE_OBJ, Json.obj("message" -> "Profile can be modified only by creator."))
      case Some(profile) =>
        val modifiedSkills = profile.skills.getOrElse(List[ProfileSkill]()).::(ProfileSkill(
          skill._id.get.stringify, false, skill.skill_name, Some(0), 0))
        log.debug(s"modifiedSkills: $modifiedSkills")

        ProfileDao.update(profile._id.get.stringify, DBQueryBuilder.set("skills", modifiedSkills)) flatMap {
          case Right(b) => Future(BaseServiceO.success())
          case Left(e) => Future(BaseServiceO.error(ERROR_UPDATE_OBJ, e.message))
        }

      case _ => Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, s"Profile[id = $profile_id] has not been found."))
    }
  }

  //todo: create JobRequest like we did profileRequest and move validation to controller after that.s
  def addJob(profileId: String, userId: String, data: Map[String, JsValue]): Future[JsObject] = {
    ProfileDao.findActiveById(profileId) flatMap {
      case Some(profile) if profile.getCreatorUserId != userId =>
        Future(BaseServiceO.error(ERROR_UPDATE_OBJ, "Job can be modified only by creator."))
      case Some(profile) =>
        val company_name = data.get("company_name").get.as[String]
        val occupation = toCamelCase(data.get("occupation").get.as[String])


        //TODO: move to the formatter of model Request object (see how it's done for profile createion)
        val start_dateS = data.get("start_date").get.as[Long]
        val end_dateS = data.get("end_date").get.as[Long]

        val start_dateO = if(start_dateS ==0 ) None else Some(start_dateS)
        val end_dateO = if(end_dateS ==0 ) None else Some(end_dateS)

        /*
        val startDate = start_dateS match {
          case "" => None
          case x => Option(x.toLong)
        }
        val endDate = end_dateS match {
          case "" => None
          case x => Option(x.toLong)
        }*/

        //val startDate = data.get("start_date").get.as[Long]
        //val endDate = data.get("end_date").get.as[Long]

        (Utils.validInput(occupation) && Utils.validInput(company_name)) match {
          //validation
          case false => Future(BaseServiceO.error(ERROR_VALID_INPUT_LIMIT, s"Size of the occupation or company name is not supported."))
          case true => {

            val newCompanies = new Company(Some(company_name),None,None) :: Nil
            val modifiedJobs = profile.jobs.
              ::(new Job(Some(newCompanies), Some(occupation), start_dateO, end_dateO, Some(end_dateO.isEmpty)))

            ProfileDao.updateJobs(profile._id.get.stringify,modifiedJobs)

          }
        }

      case _ => Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, s"Profile[id = $profileId] has not been found."))
    }
  }

  def getPopularProfilesWithoutCache(offset: Int, tags:List[String]): Future[List[JsObject]] = {
    log.trace(s"getPopularProfilesWithoutCache, offset: $offset")

    val popularProfilesF = ProfileDao.findPopular(offset,POPULARS_FETCH_SIZE, tags)

    popularProfilesF.map {
      list => list map { p => //iterate over profiles

        val profileId = p._id.getOrElse(BSONObjectID("0")).stringify

          Json.toJson[PopularProfileDto](PopularProfileDto(
            Some(profileId),
            p.name,
            p.getInitials,
            p.getTheme,
            p.getSmallImageUrl,
            p.comments_count.getOrElse(0),
            p.getViewCount, //views count
            PopularJobDTO(p.getLastOccupation,p.getLastCompanyName),
            p.getAverageRank,
            p.getPopularInd
          )
          ).as[JsObject]
      }
    }
  }


  def popularProfiles(offset: Int, tags: List[String]): Future[List[JsObject]] = {
    log.trace(s"popularProfiles, offset: $offset")
    getPopularProfilesWithoutCache(offset,tags)

  }



  //return previous value for this user for General
  def getGeneralPreviousVote(previousProfileVotesByUser: List[Vote]): Option[Int] = {
    log.trace(s"previousProfileVotesByUser")



    val generalVote:Option[Vote] =previousProfileVotesByUser.find(_.skill_id.isEmpty)
    val res:Option[Int] = generalVote match {
      case Some(v) => Some(v.vote_value)
      case None => None
    }
    res

  }

  /**
   *
   * deletes profile(soft delete), deletes all bookmarks
   * @param profileId
   * @param userId
   * @return
   */
  def delete(profileId: String, userId: String): Future[JsObject] = {

    //consider resetting popular count to 0?
    val deleteBookmarksF = BookmarkService.removeAll(profileId)
    val deleteProfileF = ProfileDao.updateState(profileId, ProfileState.Deleted)


    val result = for {
      f1DeleteBookmarks <- deleteBookmarksF
      f2DeleteProfile <- deleteProfileF
    } yield f2DeleteProfile

    result map {
      case Right(b) => {
        BaseServiceO.success()
      }
      case Left(e) => BaseServiceO.error(ERROR_UPDATE_OBJ, e.message)
    }

  }

  /**
   * converts list of profile skills and skill votes to list of profile skilldto's
   * (with previous vote in it for each skill)
   * @param previousProfileVotesByUser
   * @param profileSkills
   * @return
   */
  def getProfileSkillDtos(previousProfileVotesByUser: List[Vote], profileSkills: Option[List[ProfileSkill]]): Option[List[ProfileSkillDto]] = {
    log.trace(s"getProfileSkillDtos, profileSkills")

    profileSkills match {
      case Some(pSkills) => {
        log.trace("profileSkills count" + profileSkills.get.length)
        var list = List[ProfileSkillDto]()
        profileSkills.get.map { ps =>
          val previousProfileSKillVotesByUser = previousProfileVotesByUser.filter(vote =>
            vote.skill_id.isDefined && vote.skill_id.get == ps.skill_id) //TODO try to use find instead of filter

          val previousVoteO: Option[Int] = previousProfileSKillVotesByUser match {
            case Nil => None
            case _ => {
              log.trace("previous skill vote " + previousProfileSKillVotesByUser.head.vote_value)
              Some(previousProfileSKillVotesByUser.head.vote_value)
            }
          }
          list = ProfileSkillDto(ps.skill_id, ps.is_custom, ps.skill_name, ps.skill_average_rank.getOrElse(0), ps.votes_count, previousVoteO) :: list

        }
        Some(list)
      }
      case None => None
    }
  }

  /**
   * compare count and my previous vote for each item. If count>1 - somebody else voted, if 1 - maybe it's my vote
   * @param previousGeneralVote my previous general vote, if any
   * @param generalVotesCount - total count of votes for general profile
   * @return true if anyone else(besides me) voted for profile or skill, false otherwise
   */
  def checkIfOtherPeopleVoted(previousGeneralVote: Option[Int], generalVotesCount: Int, skillDtos: Option[List[ProfileSkillDto]]): Boolean = {
    //check if other people had general votes already
    val otherPeopleLeftGeneralVote: Boolean = generalVotesCount match {
      case 0 => false
      case 1 => {
        //if only 1 vote left(check if it's me)
        previousGeneralVote match {
          case Some(_) => false
          case None => true
        }
      }
      case _ => true
    }

    otherPeopleLeftGeneralVote match {
      case true => true //exit
      case false => {
        //same for each available skill
        skillDtos match {
          case Some(list) => checkOthersSkillVoted(list)
          case None => false
        }
      }
    }
  }

  /**
   * check if anybody else but me voted for at least one skill of the profile
   * @param listSkillDtos
   * @return
   */
  def checkOthersSkillVoted(listSkillDtos: List[ProfileSkillDto]): Boolean = {

    val res: Option[ProfileSkillDto] = listSkillDtos.find(x => (
      x.votes_count > 1 || ((x.votes_count == 1) && x.vote_value.isDefined)
      ))
    res match {
      case Some(r) => true
      case None => false
    }
  }

  def sortSkills(skillDtosO: Option[List[ProfileSkillDto]]): Option[List[ProfileSkillDto]] = {
    skillDtosO match {
      case Some(list) => {
        Some(list.sortWith((a, b) => {
          (a.votes_count == b.votes_count) match {
            //if both have the same count - sort by name
            case true => (a.skill_name.compareTo(b.skill_name) < 0)
            //otherwise sort by votes count
            case _ => (a.votes_count > b.votes_count)
          }
        }))
      }
      case None => None
    }
  }

  def transformProfileToDto(profile: Profile,
                            userId: String = "", isBookmarkedByCurrent: Boolean = false, hasOtherPeopleComments: Boolean = false, previousProfileVotesByUser: List[Vote] = Nil) = {
    log.trace(s"transformProfileToDto")

    val previousGeneralVote: Option[Int] = getGeneralPreviousVote(previousProfileVotesByUser) //previous vote by user

    val skillDtosO = getProfileSkillDtos(previousProfileVotesByUser, profile.skills)
    val sortedSkillDtosO: Option[List[ProfileSkillDto]] = sortSkills(skillDtosO)

    //check if other people had skill votes already
    val othersVoted: Boolean = checkIfOtherPeopleVoted(previousGeneralVote, profile.getVotesCount, sortedSkillDtosO)


    //if nobody else left comments or votes and i am creator - I can delete
    val canDeleteProfile = (userId == profile.getCreatorUserId && !hasOtherPeopleComments && !othersVoted)


    profile._id.map { profileId =>


       log.debug(s"transformProfileToDto calculated rank is ${profile.getAverageRank}")
        Json.toJson[ProfileDto](ProfileDto(
          Some(profileId.stringify),
          profile.name,
          profile.getInitials,
          maskProfileEmail(profile),
          profile.getLargeImageUrl,
          profile.getCreatorUserId,
          profile.getTheme,
          profile.getViewCount,
          profile.getAverageRank,
          profile.getVotesCount,
          previousGeneralVote,//previous vote
          profile.comments_count.getOrElse(0),
          profile.getState,
          canDeleteProfile,
          isBookmarkedByCurrent,//is bookmarked by current user
          Some(userId == profile.getCreatorUserId), //this is an author of the profile
          profile.jobs,
          profile.tags,
          sortedSkillDtosO,
          profile.getPopularInd)).as[JsObject]

    }.get
  }

  /**
   * Approva or reject pending profile,
   * For approved - add to the bookmars of the creator
   * in Both cases - email creator
   * @param uid - hashcode for the confirmation associated with profileId
   * @param newState - approve or reject
   * @return profileId as a JSON response or a json error
   */
  def approvalStateChange(uid:String, newState: Int): Future[JsObject] = {
    log.trace(s"approvalStateChange, uid: $uid, state: $newState")
    ApprovalLinkDao.findOne(DBQueryBuilder.id(uid)).flatMap {
      case Some(link) => {
        val profileId =link.profileId
        ProfileDao.findAnyById(profileId) flatMap {
          case Some(profile) => {
            profile.getState match  {
              case ProfileState.Approved =>Future(BaseServiceO.error(ERROR_APPROVE_PROFILE, "Profile has been approved already"))
              case ProfileState.Rejected =>Future(BaseServiceO.error(ERROR_APPROVE_PROFILE, "Profile has been rejected already"))
              case ProfileState.Waived =>Future(BaseServiceO.error(ERROR_APPROVE_PROFILE, "Profile doesn't need an approval"))
              case _ => {//pending
                ProfileDao.updateState(profile._id.get.stringify, newState) map {
                  case Right(b) => {
                    approvalResponseNotification(profile.getCreatorUserId, profile, newState)
                    newState match {
                      case ProfileState.Approved =>BookmarkService.add(profile._id.get.stringify,profile.getCreatorUserId)
                    }
                    BaseServiceO.success(Json.obj("profileId" -> profileId))
                  }
                  case Left(e) => BaseServiceO.error(ERROR_APPROVE_PROFILE, e.message)
                }
              }
            }
          }
          case _ => Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, s"Profile[id = $profileId] has not been found."))
        }
      }
      case _ => Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND,"No associated profile found"))

    }
  }

  /**
   * gets owner's info and sends a notification to him when profile's approved or rejecter
   * @param creatorId
   * @param profile
   */
  def approvalResponseNotification(creatorId:String, profile: Profile, newState: Int) {
    log.trace(s"approvalResponseNotification, creator user_id: $creatorId, state:" + newState)
    UserDaoOld.findByUserId(creatorId) map {
      case Some(owner) =>  EmailService.sendApprovalDecision(owner, profile, newState)
      case _ => log.error(s"can't find creator's profile user_id: $creatorId")
    }
  }



  def getProfilesByIds(profileIds:List[BSONObjectID]) = {
    log.trace(s"getProfilesByIds")
    ProfileDao.find(DBQueryBuilder.query(
        DBQueryBuilder.in("_id", profileIds))) map { list =>
          list.map(profile => //iterate over profiles
            Json.toJson[SearchProfileDto](SearchProfileDto(
              Some(profile._id.get.stringify),
              profile.name,
              profile.getInitials,
              profile.getTheme,
              profile.email,
              profile.getSmallImageUrl,
              profile.comments_count.getOrElse(0),
              profile.getVotesCount,
              profile.getLastOccupation,
              profile.getLastCompanyName,
              profile.getAverageRank //profile rank

          ) ).as[JsObject]

      )
    }
  }



}
