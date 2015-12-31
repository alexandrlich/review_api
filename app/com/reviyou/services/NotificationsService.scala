package com.reviyou.services

import com.reviyou.common.Utils
import com.reviyou.models.dto.NotificationDto
import play.api.Play
import com.reviyou.services.dao._
import play.api.libs.json.Json
import com.reviyou.common.RestStatusCodes._
import com.reviyou.models._
import scala.concurrent.Future
import com.reviyou.services.db.DBQueryBuilder
import reactivemongo.bson.BSONObjectID
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject
import org.joda.time.DateTime
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Notifications feed inside of the app
 */
object NotificationsService {

  val log = LoggerFactory.getLogger(getClass)
  import java.util.concurrent.Executors
  import concurrent.ExecutionContext
  val executorService = Executors.newFixedThreadPool(Play.application.configuration.getInt("notifications.thread.pool").getOrElse(1))
  val hoursToCollect = Play.application.configuration.getInt("notifications.collect.period").getOrElse(10)

  //show all my notifications for the past X days
  def getMyNotifications(user_id:String,offset:Int): Future[List[NotificationDto]] = {
    log.trace("getMyNotifications")

    NotificationDao.find(DBQueryBuilder.query(Json.obj(Notification.followerFieldName -> user_id))
      ++DBQueryBuilder.orderBy(Json.obj(Notification.createTimeFieldName -> -1)), startFrom =offset, upTo = 50 )
    .map {
    list =>
      list.map(notification => convertNotificationToDto(notification))
  }
}

  /**
   * run batch job for all comments added for the past XX hours
   * Process profiles 1 by one. For each group and count new comments
   * and create notification for the subscribed users
   *
   * @return
   */
  def collectNotifications(): Future[JsObject] = {

    val sinceTime = DateTime.now.minusHours(hoursToCollect)

    log.info(s"collectNotifications, time: $sinceTime")

    CommentDao.findRecentComments(sinceTime).map { list =>

      val groupedByProfileMap = list.groupBy(_.profile_id)
      log.info(groupedByProfileMap.size + " profiles have new comments!")
      //only di it when you have 1 thread with custom execution context!!
      for ((profileId, newProfileCommentsList) <- groupedByProfileMap)
        processProfileComments(profileId, newProfileCommentsList)

      BaseServiceO.success(Json.obj())
    }
  }


  def getNewNotificationsCount(user_id:String): Future[Int] = {

    log.trace("getNewNotifications")
    NotificationDao.countNewByProfile(user_id)


  }

  def convertNotificationToDto(notification: Notification): NotificationDto = {
    //val recentDate  = DateTime.now().minusDays(7)
    //log.trace("convertNewsToDto, recentDate " +  recentDate)
    NotificationDto(
      notification._id.get.stringify,
      notification.profile_id,
      //notification.profile_name,
      notification.text,
      notification.follower_id,
      //notification.new_reviews_count,
      notification.create_time.toDate.getTime,
      notification.acknowledge
    )
  }

  def processProfileComments(profileId:String, newProfileCommentsList:List[Comment]) = {
    log.debug(s"processProfileComments, profileId: $profileId")
    //1 threads per future
    implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

    BookmarkService.getFollowerIds(profileId) map {list=>

        ProfileDao.findActiveById(profileId).map { profile =>
          val profileName = profile.get.name

          for (followerId <- list) {
            val count: Long = countNewCommentsToNotifyOf(newProfileCommentsList, followerId)
            addNotificationRecord(followerId, profileId, profileName, count)
          }
        }

    }
  }

  /**
   * if the follower commented by himself - count everything after that
   * otherwise - notify of all new comments(since last XX hours)
   * @param newProfileCommentsList - recent comments on profile
   * @param followerUserId
   * @return
   */
  def countNewCommentsToNotifyOf(newProfileCommentsList:List[Comment],followerUserId:String):Long = {
    val myComments = newProfileCommentsList.filter( x => x.user_id == followerUserId )

    val count  = myComments.isEmpty match {
      case false => {
        val countCommentsAfter = newProfileCommentsList.filter( x => x.create_time >myComments.last.create_time )

        if(countCommentsAfter.isEmpty) 0 else countCommentsAfter.size
      }
      case true => newProfileCommentsList.size
    }
    count
  }

  def addNotificationRecord(followerId:String,profileId:String,profileName:String, count:Long):Future[JsObject] = {
    log.trace(s"addNotificationRecord, profileId: $profileId, followerId: $followerId")

    log.debug("name:"+profileName)

    val id: BSONObjectID = Utils.generateUniqueCompositeKey(followerId,profileId)

    val text = s"Profile for $profileName has $count new comment(s)."


    val notif = Notification(
      Some(id),//pk
      followerId,
      profileId,
      text,
      DateTime.now(),
      acknowledge = false)

    NotificationDao.upsert(id.stringify,notif) flatMap {
      case Left(left) => Future(BaseServiceO.error(ERROR_INSERT_OBJ_TO_DB, left.message))
      case Right(right) => Future(BaseServiceO.success())
    }
  }

  def removeNotification(notiicationId:String, userId:String): Future[JsObject] = {
    log.trace(s"remove notiicationId: $notiicationId, userId: $userId")

    //find record to get profileId
    NotificationDao.findOne(DBQueryBuilder.and(DBQueryBuilder.id(notiicationId),
      Json.obj(Notification.followerFieldName -> userId))).flatMap {
      case Some(comment) =>
        NotificationDao.remove(
          DBQueryBuilder.and(DBQueryBuilder.id(notiicationId), Json.obj(Notification.followerFieldName -> userId)), firstMatchOnly = true
        ) flatMap {
          case Left(left) => Future(BaseServiceO.error(ERROR_DELETE_OBJ, left.message))
          case Right(right) => Future(BaseServiceO.success(Json.obj()))
        }
      case _ =>Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, "No associated notifications found"))
    }
  }

  def ackNotification(notiicationId:String, userId:String): Future[JsObject] = {
    log.trace(s"ackNotification notificationId: $notiicationId, userId: $userId")

    //find record to get profileId
    NotificationDao.findOne(DBQueryBuilder.and(DBQueryBuilder.id(notiicationId),
      Json.obj(Notification.followerFieldName -> userId))).flatMap {
      case Some(comment) =>
        NotificationDao.update(notiicationId, DBQueryBuilder.set(Notification.ackFieldName, true)) flatMap {
          case Right(b) => Future(BaseServiceO.success())
          case Left(e) => Future(BaseServiceO.error(ERROR_UPDATE_OBJ, e.message))
        }
      case _ =>Future(BaseServiceO.error(ERROR_OBJ_NOT_FOUND, "No associated notifications found"))
    }
  }


  }
