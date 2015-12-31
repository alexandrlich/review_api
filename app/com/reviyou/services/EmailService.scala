package com.reviyou.services

import com.reviyou.common.RestStatusCodes._
import com.reviyou.common.dto.SignupRequest
import com.reviyou.models.dto.ProfileRequest
import com.reviyou.models.{Profile, UserModel}
import org.slf4j.LoggerFactory
import com.sendgrid._
import play.api.Play
import play.api.Play.current
import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID

/**
 * on demand email services,
 * uses sendGrid
 * doesn't check delivery\bounce yet
 */
object EmailService {

  val log = LoggerFactory.getLogger(getClass)
  val sendOutside = Play.application.configuration.getBoolean("sendgrid.externalDistribution.enabled").get
  val testEmailReceiver = Play.application.configuration.getString("sendgrid.test.internalToAddress").get
  /**
   * Sends email requesting a profile approval for the profile's email
   * @param jsProfileRequest new pending profile
   * @param uid
   * @return
   */
  def sendProfileApprovalRequest(owner:UserModel, jsProfileRequest:ProfileRequest, uid: String):JsObject = {
     val html = views.html.profileApprovalRequest(owner,jsProfileRequest, uid)
    sendBase(
      jsProfileRequest.email,
      owner.first_name + " " + owner.last_name + " " + Play.application.configuration.getString("sendgrid.profileApproval.subject").get,
      html.toString());

  }

  /**
   * Sends email confirming approve\reject decision back to the creator
   * @param owner
   * @param profile
   * @return
   */
  def sendApprovalDecision(owner:UserModel, profile: Profile, newState: Int):JsObject= {
    val html = views.html.profileApprovalDecisionEmail(owner,profile, newState)
    sendBase(
      owner.email,
      Play.application.configuration.getString("sendgrid.profileApprovalDecision.subject").get,
      html.toString());
  }

  def sendAccountApprovalRequest(signupRequest: SignupRequest, uid:String):JsObject = {
    val html = views.html.accountApprovalRequestEmail(signupRequest, uid)
    sendBase(
      signupRequest.email,
      Play.application.configuration.getString("sendgrid.userApproval.subject").get,
      html.toString());
  }

  def sendBase(emailToExternal: String,subject: String, htmlStr: String):JsObject = {
    log.debug(s"sendAccountApprovalRequest to email: $emailToExternal")
    val sendgrid  = new SendGrid(
      Play.application.configuration.getString("sendgrid.user").get,
      Play.application.configuration.getString("sendgrid.password").get
    );

    var mail = new SendGrid.Email();


    val emailDestination = sendOutside match {
      case true => emailToExternal
      case  _ =>{
        log.debug("NOT PROD: sending email to our test address")
        testEmailReceiver
      }
    }

    mail.addTo (emailDestination)
    mail.setFrom(Play.application.configuration.getString("sendgrid.from").get);

    mail.setSubject(subject);
    mail.setHtml(htmlStr);
    val response: SendGrid.Response  = sendgrid.send(mail);

    log.debug("status: " + response.getStatus + " message: " + response.getMessage)
    //response.getStatus
    //(response.getStatus, response.getMessage)
    response.getStatus match {
      case false => BaseServiceO.error(ERROR_EMAIL_DELIVERY,"can not email a request to the creator, error:  " + response.getMessage)
      case _ => BaseServiceO.success()
    }

  }


  def sendAccountPassResetRequest(email:String,  uid: String):JsObject = {
    val html = views.html.accountPassResetRequestEmail(uid)
    sendBase(
      email,
      Play.application.configuration.getString("sendgrid.userPassResetApproval.subject").get,
      html.toString());

  }
}
