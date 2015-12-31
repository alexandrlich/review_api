package com.reviyou.services

import com.reviyou.common.dto.DeviceLoginData
import com.reviyou.services.dao._
import org.joda.time.DateTime
import play.api.libs.json.Json
import com.reviyou.common.RestStatusCodes._
import scala.concurrent.Future
import com.reviyou.services.db.DBQueryBuilder
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject
import play.api.libs.concurrent.Execution.Implicits._


object DevicesPushIntegrationService {

  val log = LoggerFactory.getLogger(getClass)

  //show all my notifications for the past X days
  def register(data:DeviceLoginData): Future[JsObject] = {
    log.trace("register")


    DevicesPushIntegrationDao.upsert(Json.obj("uuid" -> data.uuid),
      DBQueryBuilder.set(
        Json.obj("token" -> data.notificationToken.get,
            "model" -> data.model,
            "platform" -> data.platform,
            "device_version" -> data.version,
            "device_active" ->true
        )
      )
    ) flatMap {
        case Right(vote) => Future(BaseServiceO.success())
        case Left(error) => Future(BaseServiceO.error(ERROR_UPDATE_OBJ, error.message))
    }
  }

  /**
   * feedback from Apple\Android about inactive devices
   * @param token
   * @param inactiveDate
   * @return
   */
  def markDeviceInactive(token:String,inactiveDate:DateTime): Future[JsObject] = {
    log.trace("register")


    DevicesPushIntegrationDao.upsert(Json.obj("token" -> token),
      DBQueryBuilder.set(
        Json.obj(
          "device_active" ->false,
          "inactive_since"-> Some(inactiveDate)
        )
      )
    ) flatMap {
      case Right(vote) => Future(BaseServiceO.success())
      case Left(error) => Future(BaseServiceO.error(ERROR_UPDATE_OBJ, error.message))
    }
  }

  def associateUser(user_id:String,uuid:String) = {
    log.trace(s"associateUser, user_id:$user_id, uuid: $uuid")
    DevicesPushIntegrationDao.findAndModify(Json.obj("uuid" -> uuid),
      Json.obj("user_id" -> user_id)

    ) flatMap {
      case Right(vote) => Future(BaseServiceO.success())
      case Left(error) => Future(BaseServiceO.error(ERROR_UPDATE_OBJ, error.message))
    }
  }

  /**
   *
   * @param user_id
   * @return list of(notificationToken+devicePlatform)
   */
  def getPushTokens(user_id:String):Future[List[(String,String)]] = {
    log.trace(s"getPushToken, user_id:$user_id")

    DevicesPushIntegrationDao.find(DBQueryBuilder.query(Json.obj("user_id" -> user_id))).map { list =>
      //log.debug("found:" + list)
      list.map { item =>  (item.token,item.platform) }
    }
  }



  }
