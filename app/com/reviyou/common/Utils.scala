package com.reviyou.common

import com.reviyou.models.Profile
import org.slf4j.LoggerFactory
import reactivemongo.bson.BSONObjectID


/**
 * Created by ALEXANDR on 4/19/14.
 */
object Utils {

  val log = LoggerFactory.getLogger(getClass)


  def maskProfileEmail(profile: Profile): Option[String] = {
    if (!profile.email.isDefined) {
      return None
    }
    val email = profile.email.get
    if (email.indexOf('@') > 3) {
      val pos = email.indexOf('@') - 2
      val maskedPart = email.substring(1, pos)
      val mask = maskedPart.toList.map(x => "*").mkString

      Some(email.replace(maskedPart, mask.toString))
    } else if(email.indexOf('@') > 0){
      val maskedPart = email.substring(0, email.indexOf('@'))
      val mask = maskedPart.toList.map(x => "*").mkString
      Some(email.replace(maskedPart, mask.toString))

    } else {
      Some(email)
    }

  }

  def validInput(str: String):Boolean = {
    return str.size<1000
  }

  def generateUniqueCompositeKey(followerId:String,profileId:String): BSONObjectID = {
    val str = (followerId+profileId).hashCode.toString

    //fill out to make 24 characters
    val idstr = str.length<24 match {
      case true =>List.fill(24-str.length)("f").mkString+str
      case false => {str.substring(24)}
    }
    log.trace(s"generated id: $idstr")
    BSONObjectID(idstr)

  }
}
