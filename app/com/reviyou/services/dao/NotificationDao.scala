package com.reviyou.services.dao

import com.reviyou.models.Notification
import com.reviyou.services.db.DBQueryBuilder
import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.Count

import scala.concurrent.Future

/* Implicits */

//import play.modules.reactivemongo.json.ImplicitBSONHandlers._


object NotificationDao extends DocumentDAO[Notification] {

  val collectionName = "notifications"


  def countNewByProfile(userId: String): Future[Int] = {
    log.debug(s"countNewByUser[user_id=$userId]")

    val query = BSONDocument(Notification.followerFieldName -> userId, Notification.ackFieldName ->false)
    val command = Count(collectionName, Some(query))

    db.command(command) // returns Future[Int]
  }

  def getLatestNotifications(sinceTime:DateTime):Cursor[Notification] = {


    val query = DBQueryBuilder.query(DBQueryBuilder.gt(Notification.createTimeFieldName, sinceTime.getMillis))
    getCursor(query
        ++DBQueryBuilder.orderBy(Json.obj(Notification.followerFieldName -> 1)))
  }

}
