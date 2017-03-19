package services

import javax.inject._
import dal.HotelRepository
import models.Hotel

import scala.concurrent.Future

/**
  * Created by style on 20/03/17.
  */

trait HotelService {
  def getHotels(city: String, order: String): Future[Seq[Hotel]]
}

@Singleton
class HotelServiceImpl @Inject() (repo: HotelRepository) extends HotelService{

  def getHotels(city: String, order: String): Future[Seq[Hotel]] = {

    order match {
      case "asc" => if(city == "") return repo.listHotelOrderByPriceASC() else repo.getHotelsByCityOrderByPriceASC(city)
      case "desc" => if(city == "") return repo.listHotelOrderByPriceDESC() else repo.getHotelsByCityOrderByPriceDESC(city)
    }

  }

}
