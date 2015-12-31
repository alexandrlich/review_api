package com.reviyou.services.dao

import com.reviyou.models.Feedback
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._

/**
 * Created by zhgirov on 02.05.14.
 */
object FeedbackDao extends DocumentDAO[Feedback] {

  val collectionName = "feedbacks"

}
