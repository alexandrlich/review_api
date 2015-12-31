package com.reviyou.services.dao

import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending
import com.reviyou.models.ProfileVisitors

/**
 * Created by zhgirov on 17.06.14.
 */
object ProfileVisitorsDao extends DocumentDAO[ProfileVisitors] {

  val collectionName = "profileVisitors"


}
