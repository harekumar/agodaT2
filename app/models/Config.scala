package models

import play.api.libs.json.Json

/**
  * Created by style on 21/03/17.
  */
case class Config (id: Long, token: String, apiLimit: Int)

object Config {

  implicit val configFormat = Json.format[Config]
}
