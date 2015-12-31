package com.reviyou.services.dao

import com.reviyou.models.DevicesPushIntegration

/* Implicits */

import play.modules.reactivemongo.json.ImplicitBSONHandlers._


object DevicesPushIntegrationDao extends DocumentDAO[DevicesPushIntegration] {

  val collectionName = "devicesPushIntegration"



}
