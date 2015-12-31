package com.reviyou.common.dto

import com.reviyou.common.CustomException
import com.reviyou.models.{UserCredencials, LoginProvider}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

case class SignupRequest(
  first_name:String,
  last_name: String,
  email: String,
  password: String
  ) {

}




object SignupRequest  {

  //needed here duplicated with case class because of the parses


  //should be the same order as fields in constructor
  implicit val loginRequestDataWrites = (
      (__ \ "first_name").write[String] and
      (__ \ "last_name").write[String] and
      (__ \ "email").write[String] and
      (__ \ "password").write[String]
    )(unlift(SignupRequest.unapply))

      implicit val loginRequestDataReads = (
          (__ \ "first_name").read[String] and
          (__ \ "last_name").read[String] and
          (__ \ "email").read[String] and
          (__ \ "password").read[String]
        )(SignupRequest.apply _)
}
