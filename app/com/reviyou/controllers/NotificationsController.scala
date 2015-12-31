package com.reviyou.controllers

import com.reviyou.common.dto.DeviceLoginData
import com.reviyou.controllers.actions.{LoggingAction, AuthenticatedAction}
import com.reviyou.services._
import com.reviyou.services.dao.DevicesPushIntegrationDao
import com.reviyou.services.db.DBQueryBuilder
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scala.concurrent.Future
import com.reviyou.common.{CustomException, RestStatusCodes}
import com.reviyou.models.Notification._


//Implicit
import play.api.libs.concurrent.Execution.Implicits._


object NotificationsController extends BaseController {

  val log = LoggerFactory.getLogger(getClass)


  /**
   * generate notifications
   * @return
   */
  def runGeneration() = LoggingAction.async {
    log.trace("run");

    NotificationsService.collectNotifications().map (res=>response(res))
  }



  def getMyNotifications(user_id: String, user_token: String, offset: Int) = AuthenticatedAction.async {
    log.trace(s"getMyNotifications, user_id:$user_id, offset: $offset")
    NotificationsService.getMyNotifications(user_id,offset) map {
      result => successResponse(
        JsArray(result.map(notification => Json.toJson(notification)))
      )
    }
  }

  def getNewNotificationsCount(user_id: String) = LoggingAction.async {
    log.trace(s"getNewNotificationsCount, user_id:$user_id")
    NotificationsService.getNewNotificationsCount(user_id).map {count=>

      successResponse(Json.obj("newNotificationsCount" -> count.toString))


    }

    }

  def remove(notificationId: String, userId: String) = AuthenticatedAction.async { request =>
    log.trace(s"delete notificationId: $notificationId, userId: $userId")
    NotificationsService.removeNotification(notificationId, userId).map(res => response(res))
  }

  def ask(notificationId: String) = AuthenticatedAction.async(parse.json) { request =>
    val userId = request.body.as[JsObject].value.get("user_id").get.as[String]
    log.trace(s"ask notificationId: $notificationId, userId: $userId")

    NotificationsService.ackNotification(notificationId, userId).map(res => response(res))


  }


  def registerPushNotification() = LoggingAction.async(parse.json) {request=>
    log.trace(s"registerPushNotification")

    import com.reviyou.common.dto.LoginRequest._

    val pushNotificationRequest: DeviceLoginData =
        request.body.validate[DeviceLoginData].map(r => r).recoverTotal{
          log.trace("request: " + request.body.toString())
          e => {
            throw CustomException.create(RestStatusCodes.REQUEST_PARSING_EXCEPTION, "Can not parse login request")
          }
        }

    log.trace("result: " + pushNotificationRequest.notificationToken.get)

    DevicesPushIntegrationService.register(pushNotificationRequest)

      Future.successful(successResponse(Json.obj()))

  }


  /**
   * push notifications to apple\ios
   */
  def pushRemoteNotifications()= LoggingAction.async {
    log.trace("pushRemoteNotifications");

    RemotePushNotificationsService.pushNotificationsProcessor().map (res=>response(res))
  }


}