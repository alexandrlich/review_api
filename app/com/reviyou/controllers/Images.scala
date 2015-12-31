package com.reviyou.controllers

import play.api.libs.json.Json

import scala.concurrent._
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.api.Play
import play.api.Play.current

import scala.concurrent.duration.Duration
import scala.xml.Null
import reactivemongo.api.gridfs._

import com.reviyou.services.{ImageDao}

//{DefaultFileToSave, ReadFile, GridFS}
import reactivemongo.bson._
import play.modules.reactivemongo._//MongoController
import reactivemongo.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.io.{FileOutputStream, FileInputStream, InputStream, OutputStream, ByteArrayOutputStream, ByteArrayInputStream,PipedInputStream,PipedOutputStream}
import java.net.URL
import java.net.HttpURLConnection
import org.slf4j.LoggerFactory
import scala.util.{Success, Failure}
import play.api.libs.iteratee.Enumerator
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.github.sardine.SardineFactory;
import com.github.sardine.DavResource;
//import org.iotools.streams.InputStreamFromOutputStream;


// /needed even though idea doesn't complain about it
import reactivemongo.api.gridfs.Implicits._
/**
 * Created by ALEXANDR on 4/9/14.
 */
object Images  extends Controller  with MongoController {


  def gfs = GridFS(db,"profileImage")

  val log = LoggerFactory.getLogger(getClass)

  //val imgProcessingTimeout = Play.application.configuration.getInt("images.wait.timeout").get


  /**
   * called by nginx during mobile image request if webserver doesn't have it
   * [email][bsonid].png
   * examlpe: sashaxxx@gmail.com54bb470107234285b23ffa73.png
   * @param hash_name
   * @return
   */
  def getAttachment(hash_name: String) = Action.async { request =>
    log.trace("getAttachment");

    doGetAttachment(hash_name)
 }




  //get attachment by using _id=filename+timestamp
  /*
  if we got to this point it means most likely image can't be found on the server
  (nginx rules didn't find it) so we need to upload it to webdav and serve to the user
   */

  def doGetAttachment(file_name: String) =   {
    log.trace(s"doGetAttachment ${file_name}");

      if(file_name.size<20) {
        log.warn(s"image name is too short: ${file_name}")
        Future(NotFound("file.unavailable"))
      } else {
        //save to webserver

       val maybeFile: Cursor[ReadFile[_<:BSONValue]] = gfs.find(ImageDao.getBsonIdFromFileName(file_name));
       val maybeFileF = maybeFile.headOption
        val result = for {
          f0<-maybeFileF
          webSaveResult_f2 <-saveToWebServer(f0, file_name)
          f3<-serveResultOnUI(webSaveResult_f2,maybeFile, file_name, f0)
        } yield f3

        maybeFileF.onFailure {
          case e: Exception => {
            log.error(s"got an error during save: ${e.getMessage}");
            Future(NotFound ("file.unavailable"));
          }

        }

        result
      }
  }

  /**
   * server image to the user
   * @param saveToWebResult
   * @param cursor
   * @param file_name
   * @return
   */
  def serveResultOnUI(saveToWebResult:Boolean, cursor:Cursor[ReadFile[BSONValue]], file_name: String, fileO: Option[ReadFile[BSONValue]]): Future[SimpleResult]= {
    log.trace(s"####now serve image for the client: ${file_name}, saveToWebResult: $saveToWebResult")
    fileO match {
      case Some(f) => {
        serve(gfs, cursor, dispositionMode = "inline") recover {
          case e => {
            log.error (s"error during image ${file_name} serve: ${e.toString}")
            //TODO instead return default image here maybe?
            NotFound ("file.unavailable")
          }

        }
      }
      case None => {
        log.warn (s"we don't have file $file_name neither in db or webserver")
         //todo: serve default
        Future(NotFound ("file.unavailable"))
      }
    }

  }




  //id = filrname+creationdata
  /**
   * TODO: detailed doc on what's happening here
   * @return true if save is successful
   */
  def saveToWebServer(fileO: Option[ReadFile[BSONValue]], fileName: String) :Future[Boolean] = {
      log.trace(s"saveToWebServer ${fileName}");

      val res= fileO match {
          // we found a file
          case Some(file) => {
            log.trace(s"fetching contents of file ${file.filename} length ${file.length}b)...");
            val isO = convertStreamAndSave(file, fileName);
            isO match {
              case Some (is) => {
                val res = ImageDao.saveToWebDav(fileName, is);
                return res;
              } case None => {
                log.warn(s"file not converted from oustream, fileName: $fileName");
                return Future(false);
              }
            }

          }
          case None => {
            log.warn(s"file not found anywhere,nothing to save to dev server, fileName: $fileName");
            return Future(false);
          }
        }
    //convert outputstream to input stream with https://code.google.com/p/io-tools/wiki/Tutorial_EasyStream
    return res;
  }

  /**
   * Read from input stream, write to output stream and send ostream to webserver for save
   * @param file
   * @param fileName
   * @return true if save is successful
   */
  def convertStreamAndSave(file: ReadFile[_ <: BSONValue], fileName: String):Option[InputStream] = {
    //ATTENTION! this is not the best idea and only works for the use case when we copy to webdav and on small images only
    //better approach is to use googlecode InputStreamFromOutputStream for xample
    log.trace(s"convertStreamAndSave $fileName")
    log.trace("test" + file.length)


    val os = new ByteArrayOutputStream(file.length)
    val readToOutputStreamF = gfs.readToOutputStream(file, os).recover{
      case to: Exception => {
        log.warn(s"recover exception:" + to.getMessage)
        return None;
      }
    };


    readToOutputStreamF.onFailure {case t =>
      log.trace(s"1114..." + os.toByteArray.length)
      os.close()
      log.warn(s"return no image")
      return None
    }

    import scala.concurrent._
    import scala.concurrent.duration._

    //we must use wait to avoid chunked exception- async way doesn't work
    val actual = Await.result(readToOutputStreamF, Duration(10000, MILLISECONDS))
        actual match {

          case _ => {
              log.warn(s"returned _")
            val istream: ByteArrayInputStream = new ByteArrayInputStream(os.toByteArray);
            return Some(istream)
          }
        }

  }




/*
    //just a working example from https://github.com/sgodbillon/reactivemongo-demo-app
  //should not be used in the application
  def upload = Action.async(parse.multipartFormData) {implicit request =>
    doUploadSingle(request)
  }

  //tmp method just for test dev purposes
  def showTestImage() = Action {
    val app = Play.application
    var file = Play.application.getFile("pics/pic.jpg")
    val source = scala.io.Source.fromFile(file)(scala.io.Codec.ISO8859)
    val byteArray = source.map(_.toByte).toArray
    source.close()

    Ok(byteArray).as("image/jpeg")
  }


    //tmp method just for test dev purposes
  def showTestImage2() = Action.async {


      val futureFile = gfs.find(BSONDocument("filename" -> "sashany12@gmail.com_BSONObjectID(\"5427109f072342450030a5f4\")"))
      //val futureFile = gfs.find(BSONDocument("filename" -> "TestImageFileToSave"))
      serve (gfs,futureFile, dispositionMode = "inline") recover {
      case e =>
        InternalServerError("file.unavailable")
    }
  }




  //tmp method just for test dev purposes
  def uploadTestImage() = Action.async{ request=>

    val imageFromFB="https://graph.facebook.com/of.angelina/picture?width=100&height=100"
    val file1 =  new URL(imageFromFB)
    val fileIS:InputStream = file1.openStream()

    val connection = file1.openConnection.asInstanceOf[HttpURLConnection]
    //connection.setRequestMethod("HEAD");
    //connection.connect();

    val contentType = connection.getContentType()
    log.trace("contentType: {}",contentType)

    try {
      //"images/jpeg"
      //contentType
      val fileToSave = DefaultFileToSave("TestImageFileToSave", Some("application/file"))
      import scala.concurrent.duration._
      val timeout = 10 seconds
      //val actual = Await.result(gfs.writeFromInputStream(fileToSave, new FileInputStream(fileIS)), timeout)
      val actual = Await.result(gfs.writeFromInputStream(fileToSave, fileIS), timeout)
      log.info("image is uploaded " + actual.filename)
      Future.successful(Ok("image uploaded"))
    } catch {
      case e: Exception =>
        Future.failed(e) //successful(InternalServerError(e.getMessage))
    }
  }


  //just a working example
  def doUploadSingle(request:Request[MultipartFormData[TemporaryFile]]) =   {
    log.trace("doUploadSingle")

   val file = request.body.files.head
    log.info("filename: {}",file.filename)
    val contentType = file.contentType
    log.trace("contentType: {}",contentType)

    try {
     //file.ref.moveTo(new File("/tmp/picture/" + file.filename),true)
     val fileToSave = DefaultFileToSave(file.filename, contentType)
     import scala.concurrent.duration._
     val timeout = 10 seconds
     val actual = Await.result(gfs.writeFromInputStream(fileToSave, new FileInputStream(file.ref.file)), timeout)
     log.info("image is uploaded " + actual.filename)
     Future.successful(Ok("image uploaded"))
    } catch {
     case e: Exception =>
       Future.failed(e) //successful(InternalServerError(e.getMessage))
   }
 }


  	def verifyWebDavResource(sardine:Sardine,storeFileName:String):Boolean = {
		try {
      log.error("hello " + sardine.exists(storeFileName));
   		val results:java.util.List[DavResource] = sardine.getResources(storeFileName);
      log.error("return true" + results.get(0).getModified() )
 			return true;
		} catch {
      case e: SardineException =>
        log.error("verifyWebDavResource" + e.toString)
      //case (_) => log.error("test2222")

      }
    log.error("return false")
		return false;
	}
*/

  //should be moved to com.reviyou.services probably
  //use id
  /*
  def (fileId: BSONValue) =   {

    import scala.concurrent.duration._
    val actualRemove = Await.result(gfs.remove(fileId), imgProcessingTimeout seconds)
    log.info(s"file ${fileId} is removed ${actualRemove.n} times")
    actualRemove.n
  }*/

  //just for test purposes
  /*
  def doRemoveAttachment(fileName: String) =   {
    import scala.concurrent.duration._
    val actualRemove = Await.result(gfs.remove(BSONObjectID(fileName)), imgProcessingTimeout seconds)
    log.info(s"file ${fileName} is removed ${actualRemove.n} times")
    actualRemove.n
  }*/

  //should be moved to com.reviyou.services
  /*
    def doFindAttachmentId(fileName: String) :Option[BSONValue] =   {
    log.info("doFindAttachmentId " + fileName)

    val futureFile = gfs.find(BSONDocument("filename" -> fileName)).collect[List]()
    import scala.concurrent.duration._

    val actual = Await.result(futureFile, imgProcessingTimeout seconds)
    actual match {
      case Nil  => None
      case _ =>{
        log.info("found file " + actual.head.filename)
        //explicit cast
        Option(actual.head.id.asInstanceOf[BSONValue])
      }
    }


  }
*/
}
