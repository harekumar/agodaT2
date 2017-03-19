package models

import play.api.libs.json.Json

/**
  * Created by style on 19/03/17.
  */

case class City(id: Long, name: String)

object City {
  implicit val cityFormat = Json.format[City]
}
