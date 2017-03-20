package services

import javax.inject.{Inject, Singleton}

import dal.ConfigRepository
import models.Config
import play.api.cache.CacheApi

import scala.concurrent.ExecutionContext

/**
  * Created by style on 21/03/17.
  */

trait ConfigService {
  def getConfigByToken(token: String): Unit
}

@Singleton
class ConfigServiceImpl @Inject()(repo: ConfigRepository, cache: CacheApi)(implicit ec: ExecutionContext) extends ConfigService{

  def getConfigByToken(token: String): Unit = {

    var config:Config = null
    repo.getConfigByToken(token).map { c: Seq[Config] =>
      config = c.toList(0)
      cache.set(s"config:$token", config.apiLimit)
    }
  }
}
