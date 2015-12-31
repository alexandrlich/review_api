package com.reviyou.controllers

import org.slf4j.LoggerFactory
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import play.api.mvc._

import org.specs2.mock.Mockito
import org.specs2.runner._
import org.specs2.mutable.Specification

import play.api.mvc.MultipartFormData.FilePart
import play.api.test.WithApplication
import org.junit.runner.RunWith
import play.api.test.Helpers._
import com.reviyou.controllers.Images
import reactivemongo.bson.BSONValue
import scala.Some
import com.reviyou.utils.MongoDBTestUtils._


/**
 * Created by ALEXANDR on 4/9/14.
 */
@RunWith(classOf[JUnitRunner])
class ImagesControllerSpec extends Specification with Mockito  {

  private val log = LoggerFactory.getLogger(getClass)

  //test for working example
  /*
    "example: image upload" in withMongoDb  {implicit app =>
      println("----------------image upload test -------------------------------" )
      val request = mock[Request[MultipartFormData[TemporaryFile]]]
      //val request = mock(classOf[Request[MultipartFormData[TemporaryFile]]])
      val tempFile = TemporaryFile("do_upload","spec")
     // val fileName = "testFile.txt"
      //val part = FilePart("key: String", fileName, None, tempFile)
      val part = FilePart[TemporaryFile](key = "image", filename = "test.png", contentType = Some("image/jpeg"), ref = tempFile)

      val files = Seq[FilePart[TemporaryFile]](part)
      val formData = MultipartFormData(dataParts = Map[String, Seq[String]](), files, badParts = Seq(),missingFileParts = Seq())

      request.body returns formData

     val result = Images.doUploadSingle(request)
      status(result) must beEqualTo(OK)
      println(s"test file ${part.filename} is uploaded to db")
      var result2 = Images.doGetAttachment(part.filename)
      println("test file is retrieved from db")
      status(result2) must beEqualTo(OK)
      //TODO: test file is retrieved and match original one
        println(result2.toString)

      //remove created file
      val res :Option[BSONValue]  =Images.doFindAttachmentId(part.filename)
      //println( res.isEmpty)
      res.isEmpty mustEqual false
      val removeResult = Images.doRemoveAttachment(res.orNull)
      removeResult mustEqual 1
      //making sure it can't be loaded again
      val res2 :Option[BSONValue]  = Images.doFindAttachmentId(part.filename)
      res2 must beNone

    }
    */

}
