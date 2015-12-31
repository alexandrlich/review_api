package com.reviyou.services


import com.notnoop.apns.{APNS, ApnsService, PayloadBuilder}
import com.reviyou.models._
import com.reviyou.services.dao._
import dispatch._
import org.joda.time.DateTime
import org.json4s
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.Cursor

import scala.concurrent.Future


//end

/**
 * Apple and android push remote notofications
 */
object RemotePushNotificationsService {

  implicit val formats = org.json4s.DefaultFormats

  val log = LoggerFactory.getLogger(getClass)
  import java.util.concurrent.Executors

  import concurrent.ExecutionContext
  val executorService = Executors.newFixedThreadPool(Play.application.configuration.getInt("notifications.thread.pool").getOrElse(1))
  val pastHoursToPush = Play.application.configuration.getInt("notifications.push.period").getOrElse(24)

  val appleCertName = Play.application.configuration.getString("notifications.push.appleCertificateName").getOrElse("")
  val appleCertPassword = Play.application.configuration.getString("notifications.push.appleCertificatePassword").getOrElse("")
  val appleUseProductionServer = Play.application.configuration.getBoolean("notifications.push.appleUserProd").getOrElse(false)
  val androidApiKey = Play.application.configuration.getString("notifications.push.googleApiKey").getOrElse("")


  def pushNotificationsProcessor(): Future[JsObject] = {
    val sinceTime = DateTime.now.minusHours(pastHoursToPush)
    var prevFollowerId: String = ""
    var countProfilesPerUser: Int = 0
    var firstItem = true

    //1 threads per future
    implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

    log.info(s"pushNotificationsProcessing state ($pastHoursToPush past hours)")

    val cursor: Cursor[Notification] = NotificationDao.getLatestNotifications(sinceTime)

      def apnsService = {
        val baseBuilder =
          APNS.newService()
            //.asBatched()
            //asPool(executor, numberofConn)
            .withCert(getClass.getClassLoader.getResourceAsStream(appleCertName), appleCertPassword)

        if (appleUseProductionServer) {
          baseBuilder.withProductionDestination()
        } else {
          baseBuilder.withSandboxDestination()
        }
      }.build

    try {
      apnsService.start()
      log.info("Test apple connection")
      apnsService.testConnection()

    } catch {
      //NetworkIO
      case ex: Exception => log.error("ios push exception"  + ex.getMessage)
    }

    var countRecords = 0
    var countUsers = 0
        //group results by followerId and count profilesChanges for each
        val processDocuments: Iteratee[Notification, Unit] =
          Iteratee.foreach { item =>


            if(item.follower_id ==prevFollowerId || firstItem) {
              countProfilesPerUser+=1
              if(firstItem) {firstItem = false; prevFollowerId = item.follower_id}
            } else {
              countUsers+=1;
              sendNotificationsToUser(prevFollowerId,countProfilesPerUser+1, apnsService)
              countProfilesPerUser = 0
              prevFollowerId = item.follower_id
            }
            countRecords+=1;

          }

      val end =  cursor.enumerate(Int.MaxValue,stopOnError = false).apply(processDocuments) // returns Future[Unit]
      end.map {e=>
        try {
          apnsService.stop()
        } catch {
          case ex: Exception => log.error("ios push exception"  + ex.getMessage)
        }
        log.info(s"Number of notification records processed: $countRecords")
        log.info(s"Number of users served: $countUsers")

      }

    Future.successful(BaseServiceO.success(Json.obj()))
  }

  /**
   * Get associated devices fro the user and send notifications to all of them for ios\android
   * @param userId
   * @param countProfiles
   * @param apnsService
   * @return
   */

  def sendNotificationsToUser(userId:String, countProfiles:Int, apnsService:ApnsService) = {
    DevicesPushIntegrationService.getPushTokens(userId).map {list=>
      list map { item =>
        val deviceToken = item._1
        item._2.equalsIgnoreCase("ios") match {
          case true =>pushOneToApple(countProfiles, apnsService, deviceToken)
          case false =>pushOneToAndroid(countProfiles, deviceToken)
        }
      }
    }
  }




    //TODO later: check for inactive devices so we can mark them in our db
    /*
    Map<String, Date> inactiveDevices = service.getInactiveDevices();
for (String deviceToken : inactiveDevices.keySet()) {
    Date inactiveAsOf = inactiveDevices.get(deviceToken);
    ...
}
     */





  def pushOneToApple(commentedProfiles:Int, service: ApnsService, deviceToken:String) = {
    log.trace(s"pushToApple, deviceToken: $deviceToken")
    var payloadBuilder: PayloadBuilder = APNS.newPayload()

      payloadBuilder = payloadBuilder.badge(1)
      payloadBuilder = payloadBuilder.sound("beep.wav")
      payloadBuilder = payloadBuilder.alertBody(commentedProfiles + " new comment(s) posted to the profiles you follow!")

      val payload:String = payloadBuilder.build()
      try {
        service.push(deviceToken, payload)
      } catch {
        //NetworkIO
        case ex: Exception => log.error("ios push exception"  + ex.getMessage)
      }
  }


  /**
   * HTTP call one per notification for now
   * https://developers.google.com/cloud-messaging/server
   * @param commentedProfiles
   * @param deviceToken
   * @return
   */
  def pushOneToAndroid(commentedProfiles:Int,deviceToken:String) = {
    log.trace(s"pushToAndroid, deviceToken: $deviceToken")

    val message = commentedProfiles + " new comment(s) posted to the profiles you follow!"
    val body = Map("to" -> deviceToken,
      //"collapse_key" -> "collapse",
      "data" -> Map("message" -> message, "title" -> "Reviyou"))

    val jsonBody = Serialization.write(body)

    //.secure
    val request = url("https://android.googleapis.com/gcm/send")
      .setHeader("Authorization", s"key=$androidApiKey")
      .setHeader("Content-type", "application/json")
      .setMethod("POST")
      .setBody(jsonBody)

    val response : Future[Option[json4s.JValue]] = Http(request OK as.json4s.Json ).option

    response.map {
      case Some(data) => log.debug(data.toString)
      case None => log.error(s" can't authorize google url for push notifications, didn't retrieve validation response")
    }
  }
}
