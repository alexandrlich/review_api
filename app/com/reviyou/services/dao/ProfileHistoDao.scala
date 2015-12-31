package com.reviyou.services.dao

import com.reviyou.models.ProfileHisto
import scala.concurrent.Future
import reactivemongo.api.indexes.IndexType.Ascending

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._

//not used anywhere
/**
 * Created by zhgirov on 10.05.14.

object ProfileHistoDao extends DocumentDAO[ProfileHisto] {

  val collectionName = "profilesHistory"

}
 */
