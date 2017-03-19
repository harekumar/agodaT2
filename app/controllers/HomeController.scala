package controllers


import javax.inject._

import play.api.Logger
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.HotelService

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (hs: HotelService, val messagesApi: MessagesApi)
                               (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  /**
   * Create an Action to render all the listed hotels in JSON format.
   */
  def index(city: Option[String], sort: Option[String], order: Option[String]) = Action.async {

    val od:String = if(!order.isEmpty && order.isDefined) order.get else "asc"
    val c:String = if(city.isDefined && !city.isEmpty) city.get else ""

    Logger.debug("--------------------")
    Logger.debug(od)
    Logger.debug(c)
    Logger.debug("--------------------")

    hs.getHotels(c, od).map { h =>
      Ok(Json.toJson(h))
    }
  }

}
