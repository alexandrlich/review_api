package com.reviyou.services

import java.io.InputStream

import com.github.sardine.impl.SardineException
import com.github.sardine.{SardineFactory, Sardine}
import play.api.Play
import reactivemongo.api.indexes.Index

import scala.concurrent.{Await, Future}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.bson._
import org.slf4j.LoggerFactory
import reactivemongo.api.gridfs.{DefaultFileToSave, GridFS}
import scala.concurrent.duration._
import reactivemongo.api.gridfs.Implicits._
import reactivemongo.api.collections.default.BSONCollection
/**
 *  A data access object for profiles backed by a MongoDB collection 
 */

//todo: move logic here from Images.scala controller
//separate login b\w dao and ImagesService
object ImageDao {

  val webdavUser = Play.application.configuration.getString("webdav.user").get
  val webdavPass = Play.application.configuration.getString("webdav.password").get
  val webdavHost = Play.application.configuration.getString("webdav.host").get

  val log = LoggerFactory.getLogger(getClass)

  def gfs = GridFS(ReactiveMongoPlugin.db,"profileImage")
  /**
   *
   * store image in gfs in the format where filename = "[_id].png"
   * where _id is a pk of the  record(good for indexed search
   *
   * and png - we want it to be a valid image path for webdav
   *
   *
   * @param fileIO - actual file
   * @param userId - email of the user
   * @return fileName
   */
  def doUploadProfileImage(fileIO: java.io.InputStream, userId: String): Future[String] =   {
    log.trace(s"doUploadProfileImage, userId: $userId")
    //if (fileIO.isEmpty) None

    val contentType = Some("image/jpeg")
    log.trace("contentType: {}",contentType)

    val fileName = userId +".png"
    log.trace(s"fileName: $fileName")

    val fileToSave = DefaultFileToSave(fileName, contentType,id = BSONObjectID(userId))



      gfs.writeFromInputStream(fileToSave, fileIO).map {
        readFile =>  {
          //log.trace(s"return filename: ${newId.stringify}")
          fileName
        }
      }

  }


  def doRemoveProfileImage(fileName: String):Future[Boolean] = {

    (fileName.size<20) match {
      case true => {
        log.warn("fileName $fileName is too short")
        Future(false)
      }
      case _ => {

        gfs.remove(getBsonIdFromFileName(fileName)).map { _ =>
          log.debug(s"filename $fileName is deleted");
          true
        }.recover {
          case _ => {
            log.error(s"filename $fileName is NOT deleted")
            false
          }
        }
      }
    }

  }

  /**
   * file name looks like this [bsonId].png
   *
   * bsonId is a _id field in fileImages document and has 24 bytes
   * we only use bsonId to remove the doc
   * 54bb470107234285b23ffa73.png
   * @param fileName
   */
  def getBsonIdFromFileName(fileName:String): BSONDocument = {
    val bid = fileName.substring(fileName.size-28,fileName.size-4)
    log.trace("bid:" + bid);
    return BSONDocument("_id"->BSONObjectID(bid));
  }


  //###########WEB DAV###########//

  def saveToWebDav(filename:String, in:InputStream):Future[Boolean] = {
    try{

      //just for test. remove later
      //val mimeType = java.net.URLConnection.guessContentTypeFromStream(in);
      //log.debug(s"filename: $filename, mimeType: $mimeType")
      //end just for test


      log.trace(s"saveToWebDav: $filename")

      val sardine:Sardine = SardineFactory.begin(webdavUser, webdavPass);

      val firstCharPath = webdavHost + "/"+ filename(0)+"/";
      /*do NOT use it. it has a bug: first time it runs ok and creates a folder
      but second time sardine.exist throws 403 even though it's not a permissions problem)

      if (!sardine.exists(firstCharPath)) {
        log.trace("create a dir:" + firstCharPath )
        sardine.createDirectory(firstCharPath);
      }*/
      log.trace("put a file:" + (firstCharPath+filename ))

      sardine.put(firstCharPath+ filename, in);
      log.debug("file is saved successfully on webdav:" + (firstCharPath+filename ))
      return Future(true);
    } catch {
      case e @ (_ : SardineException | _ : org.apache.http.conn.HttpHostConnectException) =>
        log.error("storeFileName:" + e.toString)

        return Future(false);
    }

  }

  def removeFromWebdav(filename:String):Future[Boolean] = {
    try{
      log.trace(s"removeFromWebdav: $filename")

      val sardine:Sardine = SardineFactory.begin(webdavUser, webdavPass);

      val firstCharPath = webdavHost + "/"+ filename(0)+"/";
      log.trace("delete a file:" + (firstCharPath+filename ))

      sardine.delete(firstCharPath+ filename);
      log.debug("file is deleted successfully from webdav:" + (firstCharPath+filename ))
      return Future(true);
    } catch {
      case e @ (_ : SardineException | _ : org.apache.http.conn.HttpHostConnectException) =>
        log.error(s"error while trying to delete fileFileName: $filename  error: ${e.toString}")

        return Future(false);
    }

  }


}