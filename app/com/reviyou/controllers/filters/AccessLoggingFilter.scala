package com.reviyou.controllers.filters

import com.reviyou.models.LoggingItem
import org.joda.time.DateTime
import play.api.libs.iteratee.{Enumeratee, Iteratee}
import play.api.libs.json.Json
import reactivemongo.bson.BSONDocument
import com.reviyou.services.dao.LoggingItemDao

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.Logger
import play.api.mvc._
import play.api._

object AccessLoggingFilter extends EssentialFilter {
  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {
      val startTime = System.currentTimeMillis
      nextFilter(requestHeader).map {
        result =>
          val endTime = System.currentTimeMillis
          val requestTime = endTime - startTime
          val bytesToString: Enumeratee[Array[Byte], String] = Enumeratee.map[Array[Byte]] {
            bytes => new String(bytes)
          }
          val consume: Iteratee[String, String] = Iteratee.consume[String]()
          val resultBody: Future[String] = result.body |>>> bytesToString &>> consume
          resultBody.map {
            body => Logger.info(s"${requestHeader.method} ${requestHeader.uri}" + s" took ${requestTime}ms and returned ${result.header.status}")
              val jsonBody = Json.parse(body)
              Logger.debug(s"Response\nHeader:\n${result.header.headers}\nBody:\n${Json.prettyPrint(jsonBody)}")
              println(s"Response\nHeader:\n${result.header.headers}\nBody:\n${Json.prettyPrint(jsonBody)}")
          }
          result.withHeaders("Request-Time" -> requestTime.toString)
      }
    }
  }
}

/*def apply(next: (RequestHeader) => Future[SimpleResult])(request: RequestHeader): Future[SimpleResult] = {


  val resultFuture = next(request)

  println(s"============== Request: ${request.tags}")

  resultFuture.foreach(result => {
    LoggingItemDao.insert(
      LoggingItem(None,
        request.method,
        request.uri,
        request.remoteAddress,
        DateTime.now().getMillis,
        Some(request.queryString))
    )

    val msg = s"method=${request.method} uri=${request.uri} remote-address=${request.remoteAddress}" +
      s" status=${result.header.status} ";

    result.body.apply(Iteratee.foreach { doc =>
      println("found document: " + new String(doc))
    })
    accessLogger.info(msg)
  })

  resultFuture
}*/

