package com.reviyou.utils

import play.api._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DefaultDB
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import com.reviyou.services.dao.UserDao
import com.reviyou.models.UserModel
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import com.reviyou.common.dto.FbLoginData

/**
 * Test com.reviyou.utils for running tests with MongoDB
 */
object MongoDBTestUtils {

  /**
   * Run the given block with MongoDB
   */
  def withMongoDb[T](block: Application => T): T = {

    implicit val app = FakeApplication(
      additionalConfiguration = Map(
        "mongodb.uri" -> "mongodb://localhost/unittests",
        "bypassTokenValidation" -> true,
        "test_token" -> "alex_fake_token_to_allow_junits_execution")
    )
    running(app) {
      def db = ReactiveMongoPlugin.db
      try {
        dropAll(db) //preventive drop
        createUser(CommonUtils.USER_ID)
        block(app)
      } finally {
        //dropAll(db)
      }
    }
  }

  def dropAll(db: DefaultDB) = {
    Await.ready(Future.sequence(Seq(
      db.collection[JSONCollection]("bookmarks").drop(),
      db.collection[JSONCollection]("comments").drop(),
      db.collection[JSONCollection]("users").drop(),
      db.collection[JSONCollection]("users_history").drop(),
      db.collection[JSONCollection]("profiles").drop(),
      db.collection[JSONCollection]("profileImage.chunks").drop(),
      db.collection[JSONCollection]("profileImage.files").drop(),
      db.collection[JSONCollection]("votes").drop(),
      db.collection[JSONCollection]("profileVisitors").drop(),
      db.collection[JSONCollection]("logging_history").drop()
    )), 10 seconds)
  }

  def createUser(userId: String) = {
    UserDao.insert(UserModel(Some(BSONObjectID.parse(userId).get),
      "firstnameofmongodbtestutils", //first_name: String
      "lastnameofmongodbtestutils", //last_name: String
      None, //gender: Option[String]
      "user@mail.com", //email: String
      "facebook",//login provider
      "token",//token
      None, //bookmarks: Option[List[String]]
      Some("user_profile_image"), //user_profile_image: Option[String]
    //expiresIn,imageUrl,defaultImage,fbUserId
      Option(FbLoginData("30","http://imageUrl.png",true, "fbUserIdTestUtils")),
      None,//ggLoginData
      None)) //twLoginData

      //LoginData("token", DateTime.now().plusDays(1).getMillis))) //login_data: LoginData))
  }
}
