package com.reviyou.common

/**
 * Created by ALEXANDR on 4/25/14.
 */
object RestStatusCodes {

  val OK = 0
  val AUTHORIZATION_ERROR = 401//should it be called authentication error?
  val PERMISSIONS_ERROR = 403
  val SERVER_INTERNAL_ERROR = 500
  val REQUEST_PARSING_EXCEPTION = 10001
  val USER_UPDATE_ERROR = 10005
  val ERROR_INSERT_OBJ_TO_DB = 10006
  val ERROR_OBJ_NOT_FOUND = 10007
  val ERROR_PROFILE_EXISTS = 10008//used in mobile app, don't change
  val ERROR_DELETE_OBJ = 10009
  val ERROR_UPDATE_OBJ = 10010
  val ERROR_USR_NOT_FOUND = 10011
  val ERROR_VALID_INPUT_LIMIT = 10012
  val ERROR_COMMENT_VOTING = 10013
  val ERROR_APPROVE_PROFILE = 10014
  val ERROR_EMAIL_DELIVERY = 10015
  val ERROR_USER_ACCOUNT_EXISTS = 10016
  val ERROR_SIGN_IN_FAILED = 10017//used in mobile app, don't change

  //default
  val DEFAULT_CUSTOM_EXCEPTION = 10000
  val ERROR_OUTDATED_VERSION = 19999//used in mobile app, don't change

}
