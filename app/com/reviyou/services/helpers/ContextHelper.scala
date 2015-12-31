package com.reviyou.services.helpers

import scala.concurrent.ExecutionContext

/**
 * Helper around implicit contexts.
 *
 * @author zhgirov on 17.04.14.
 */
trait ContextHelper {

  implicit def ec: ExecutionContext = ExecutionContext.Implicits.global

}
