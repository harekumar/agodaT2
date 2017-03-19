package models

import play.api.libs.json.Json

/**
  * Created by style on 19/03/17.
  */
case class Hotel(id: Long, city: String, room: String,  price: Int)

object Hotel {

  implicit val hotelFormat = Json.format[Hotel]
}
