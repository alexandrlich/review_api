package com.reviyou.common

class CustomException(code:Long, msg: String) extends RuntimeException(msg)

object CustomException {
  def create(msg: String) : CustomException = new CustomException(RestStatusCodes.DEFAULT_CUSTOM_EXCEPTION, msg)
  def create(code:Long, msg: String) : CustomException = new CustomException(code, msg)

  def create(msg: String, cause: Throwable) = new CustomException(RestStatusCodes.DEFAULT_CUSTOM_EXCEPTION,msg).initCause(cause)
  def create(code: Long, msg: String, cause: Throwable) = new CustomException(code, msg).initCause(cause)
}
