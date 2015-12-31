package com.reviyou.services

import com.reviyou.models.ContactUs
import com.reviyou.services.dao.ContactUsDao
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

/**
 * Created by eugenezhgirov on 10/7/14.
 */
//TODO: rename to separate from Feedback=ContactUs functionality
object ContactUsService {

  val log = LoggerFactory.getLogger(getClass)

  def addJob( profile_id: String,
              company_name: String,
              start_date: Option[Long],
              end_date: Option[Long],
              occupation: String,
              user_id: String) = {

    ContactUsDao.insert(ContactUs(None,
      Json.obj(
        "operation" -> "addJob",
        "profile_id" -> profile_id,
        "company_name" -> company_name,
        "start_date" -> start_date,
        "end_date" -> end_date,
        "occupation" -> occupation,
        "user_id" -> user_id
      )))

  }

  def addCustomSkill (profile_id: String,
                      skill_name: String,
                      user_id: String) = {
    ContactUsDao.insert(ContactUs(None,
      Json.obj(
        "operation" -> "addCustomSkill",
        "profile_id" -> profile_id,
        "skill_name" -> skill_name,
        "user_id" -> user_id
      )))
  }

  def deleteProfile (profile_id: String,
                      user_id: String) = {
    ContactUsDao.insert(ContactUs(None,
      Json.obj(
        "operation" -> "deleteProfile",
        "profile_id" -> profile_id,
        "user_id" -> user_id
      )))
  }

}
