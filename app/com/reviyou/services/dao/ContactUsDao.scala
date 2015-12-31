package com.reviyou.services.dao

import com.reviyou.models._
import reactivemongo.api.indexes.IndexType.{Descending, Ascending}

import scala.concurrent.Future

/**
 * Created by eugenezhgirov on 10/7/14.
 */
//TODO: rename to separate from Feedback=ContactUs functionality
object ContactUsDao extends DocumentDAO[ContactUs] {

  val collectionName = "contact_us"

}
