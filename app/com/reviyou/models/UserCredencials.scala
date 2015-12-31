package com.reviyou.models

import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.{Json, JsObject}
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import com.reviyou.common.dto.{SignupRequest, TwLoginData, GoogleLoginData, FbLoginData}

/**
 * Every time when user resets a password we create another pending record.
 * When user confirms his change - we active it and remove a previous record
 * @param _id
 * @param first_name
 * @param last_name
 * @param email
 * @param password - current password if registration confirmed already
 * @param pending_password - used for password reset
 * @param account_approved - indicates if account is confirmed
 * @param pass_reset_approved - flag to save new password pending approval.
 * You can have active account but pending password reset
 *
 */
case class UserCredencials(
                      override var _id: Option[BSONObjectID],
                      first_name: String,
                      last_name: String,
                      email: String,
                      password: Option[String],
                      pending_password:Option[String],
                      conf_uid:Option[String],
                      account_approved: Boolean = false,
                      pass_reset_approved: Boolean = false,
                      override var updated: Option[DateTime] = None,
                      override var created: Option[DateTime] = None
                      ) extends TemporalModel {

  def getId():String = {
    _id.get.stringify
  }

  /**
   * move passowrd from pending to current, approve an account
   * @return
   */
  def approvedAccount(): UserCredencials = {
    UserCredencials(_id,
        first_name,
        last_name,
        email,
        pending_password,//password
        pending_password, //pending password,  None is not applied since update doesn't change Some to None
        conf_uid,//  None is not applied since update doesn't change Some to None
        true,//account approved
        pass_reset_approved
    )

  }

  /**
   * create hashed password and store as pending approval
   * @param newPassword
   * @param uid
   * @return
   */
  def resetPasswordRequest(newPassword :String, uid: String): UserCredencials = {
    val passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());

    UserCredencials(
      _id,
      first_name,
      last_name,
      email,
      password,//current password
      Some(passwordHash),//pending pass,None is not applied since update doesn't change Some to None
      Some(uid),//confirmation None is not applied since update doesn't change Some to None
      account_approved,
      false //needs pass approval
    )

  }

  /**
   * copy hashed password from pending to current
   * @return
   */
  def approvedPassReset(): UserCredencials = {
    UserCredencials(_id,
      first_name,
      last_name,
      email,
      pending_password,//new password
      pending_password, //pending password,  None is not applied since update doesn't change Some to None
      conf_uid,//  None is not applied since update doesn't change Some to None
      account_approved,
      true //pass approved
    )

  }


}


object UserCredencials {

  import play.modules.reactivemongo.json.BSONFormats._

  def createAccount(signupRequest :SignupRequest, uid: String): UserCredencials = {
    val passwordHash = BCrypt.hashpw(signupRequest.password, BCrypt.gensalt());

    UserCredencials(
      Some(BSONObjectID.generate),
      signupRequest.first_name,
      signupRequest.last_name,
      signupRequest.email.toLowerCase,
      None,//pass
      Some(passwordHash),//pending pass
      Some(uid),//confirmation
      false //account needs approval
    )

  }



  // For MongoDB serialization

  implicit val userCredencialsFormat = Json.format[UserCredencials]

}
