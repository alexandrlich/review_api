package com.reviyou.services.dao

import com.reviyou.common.CustomException
import com.reviyou.services.exceptions._
import org.slf4j.LoggerFactory
import play.api.{Logger, Play}
import play.api.libs.json.Writes
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.{MongoConnection, MongoDriver}
import reactivemongo.core.commands.LastError
import reactivemongo.core.errors.DatabaseException

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{ Failure, Success }
import play.api._
import play.api.libs.concurrent.Akka
import reactivemongo.api._
import reactivemongo.core.commands._
import reactivemongo.core.nodeset.Authenticate
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.{ Failure, Success }
import scala.concurrent.duration._
import play.api.Play.current

/**
 * Created by eugenezhgirov on 9/23/14.
 * Uses a separate db schema to log all the activities for the future warehouse
 */
object LoggingItemDao  {

  val uri = Play.application.configuration.getString("app.loggingdb.uri").get

  val log = LoggerFactory.getLogger(getClass)

  val conf = parseConf()
  lazy val driver = new MongoDriver

  lazy val connection = driver.connection(conf)
  val customStrategy =
    FailoverStrategy(
      initialDelay = 1 seconds,
      retries = 5,
      delayFactor =
        attemptNumber => 1 + attemptNumber * 0.5
    )

  lazy val db = DB(conf.db.get, connection,customStrategy)


  private def parseConf(): MongoConnection.ParsedURI = {
        MongoConnection.parseURI(uri) match {
          case Success(parsedURI) if parsedURI.db.isDefined =>
            parsedURI
          case Success(_) =>
            log.error(s"Missing database name in mongodb.uri '$uri'")

            throw CustomException.create(s"Missing database name in mongodb.uri '$uri'")
          case Failure(e) => throw CustomException.create(s"Invalid mongodb.uri '$uri' "+e.getMessage)

    }
  }

 lazy val collection = db.collection[JSONCollection](collectionName)

  val collectionName = "logging_history"

  def uncheckedInsert[T](document: T)(implicit writer: Writes[T]): Unit = {
    //log.debug(s"Unchecked insert: [collection=$collectionName, document=$document]")
    collection.uncheckedInsert(document)

  }

}
