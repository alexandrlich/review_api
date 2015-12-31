package com.reviyou.services.helpers

import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin

import reactivemongo.bson.{BSONObjectID, BSONValue}


/**
 * Helper around 'MongoDB' resources.
 *
 * @author zhgirov on 17.04.14.
 */
trait MongoHelper extends ContextHelper {

  def db = ReactiveMongoPlugin.db

}

object MongoHelper extends MongoHelper {

  def identify(bson: BSONValue) = bson.asInstanceOf[BSONObjectID].stringify

}
