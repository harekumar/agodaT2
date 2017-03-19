package controllers

import javax.inject._

import dal.HotelRepository
import play.api.Logger
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (repo: HotelRepository, val messagesApi: MessagesApi)
                               (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  /**
   * Create an Action to render all the listed hotels in JSON format.
   */
  def index(city: Option[String], sort: Option[String], order: Option[String]) = Action.async {

    Logger.debug("--------------------")
    Logger.debug(city.toString)
    Logger.debug(sort.toString)
    Logger.debug(order.toString)
    Logger.debug("--------------------")

    val od:String = if(!order.isEmpty && order.isDefined) order.get else "asc"

    if (city.isDefined && !city.isEmpty) {

      repo.getHotelsByCityOrderByPrice(city.get, od).map { h =>
        Ok(Json.toJson(h))
      }

    } else {
      repo.list(od).map { h =>
        Ok(Json.toJson(h))
      }
    }

  }

}
